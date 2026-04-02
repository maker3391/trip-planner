import React, { useEffect, useState, useCallback } from 'react';
import {
  APIProvider,
  Map,
  useMap,
  useMapsLibrary,
  Marker,
  Polyline,
  ControlPosition // 필요한 경우 위치 조정을 위해 추가
} from '@vis.gl/react-google-maps';

// --- 인터페이스 정의 (any 대체용) ---
interface PlacePoint {
  lat: number;
  lng: number;
  name: string;
  address: string;
}

interface Connection {
  from: number;
  to: number;
}

// MapController 전용 Props 타입
interface MapControllerProps {
  searchKeyword: string;
  path: PlacePoint[];
  connections: Connection[];
  onMapClick: (lat: number, lng: number, name: string, address: string) => void;
  onConnect: (idx: number) => void;
  selectedSource: number | null;
}

// --- MapController: 타입 구체화 ---
function MapController({ 
  searchKeyword, 
  path, 
  connections, 
  onMapClick, 
  onConnect, 
  selectedSource 
}: MapControllerProps) {
  const map = useMap();
  const placesLib = useMapsLibrary('places');

  useEffect(() => {
    if (!map || !searchKeyword.trim()) return;
    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ address: searchKeyword }, (results, status) => {
      if (status === google.maps.GeocoderStatus.OK && results?.[0]) {
        map.panTo(results[0].geometry.location);
        map.setZoom(15);
      }
    });
  }, [map, searchKeyword]);

  useEffect(() => {
    if (!map || !placesLib) return;

    // e의 타입을 구체적으로 지정 (google.maps.MapMouseEvent)
    const clickListener = map.addListener('click', (e: google.maps.MapMouseEvent | google.maps.IconMouseEvent) => {
      if (!e.latLng) return;
      const lat = e.latLng.lat();
      const lng = e.latLng.lng();

      // IconMouseEvent인 경우 placeId가 존재함
      if ('placeId' in e && e.placeId) {
        const service = new google.maps.places.PlacesService(document.createElement('div'));
        service.getDetails({ 
          placeId: e.placeId, 
          fields: ['name', 'formatted_address'], 
          language: 'ko' 
        }, (place, status) => {
          if (status === google.maps.places.PlacesServiceStatus.OK && place) {
            onMapClick(lat, lng, place.name || "장소명 없음", place.formatted_address || "");
          }
        });
        if (e.stop) e.stop();
      } else {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ location: { lat, lng }, language: 'ko' }, (results, status) => {
          if (status === google.maps.GeocoderStatus.OK && results?.[0]) {
            const addr = results[0].formatted_address;
            const name = addr.split(' ').slice(-2).join(' ') || "지정 위치";
            onMapClick(lat, lng, name, addr);
          }
        });
      }
    });

    return () => google.maps.event.removeListener(clickListener);
  }, [map, placesLib, onMapClick]);

  return (
    <>
      {path.map((point, i) => (
        <Marker 
          key={`${i}-${point.lat}`} 
          position={{ lat: point.lat, lng: point.lng }} 
          icon={selectedSource === i 
            ? "http://maps.google.com/mapfiles/ms/icons/red-dot.png" 
            : "http://maps.google.com/mapfiles/ms/icons/blue-dot.png"
          }
          onClick={() => onConnect(i)}
        />
      ))}
      {connections.map((conn, i) => (
        <Polyline key={i} path={[path[conn.from], path[conn.to]]} strokeColor="#4285F4" strokeWeight={4} strokeOpacity={0.6} />
      ))}
    </>
  );
}

// --- 메인 컴포넌트 ---
export default function MyMapApp({ searchKeyword }: { searchKeyword: string }) {
  const [path, setPath] = useState<PlacePoint[]>([]);
  const [connections, setConnections] = useState<Connection[]>([]);
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [connectSource, setConnectSource] = useState<number | null>(null);
  const [editingIdx, setEditingIdx] = useState<number | null>(null);
  const [editValue, setEditValue] = useState("");

  const handleMapClick = useCallback((lat: number, lng: number, name: string, address: string) => {
    setPath(prev => [...prev, { lat, lng, name, address }]);
  }, []);

  const saveEditedName = (idx: number) => {
    if (editValue.trim()) {
      setPath(prev => prev.map((p, i) => i === idx ? { ...p, name: editValue } : p));
    }
    setEditingIdx(null);
  };

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative' }}>
      <APIProvider apiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY} libraries={['places', 'geocoding']}>
        <Map
          style={{ width: '100%', height: '100%' }}
          defaultCenter={{ lat: 35.179, lng: 129.075 }}
          defaultZoom={13}
          mapId={import.meta.env.VITE_GOOGLE_MAP_ID}
          disableDefaultUI={true}
        >
          <MapController 
            searchKeyword={searchKeyword}
            path={path}
            connections={connections}
            onMapClick={handleMapClick}
            onConnect={(idx) => {
              if (connectSource === null) setConnectSource(idx);
              else if (connectSource === idx) setConnectSource(null);
              else {
                setConnections(prev => [...prev, { from: connectSource, to: idx }]);
                setConnectSource(null);
              }
            }}
            selectedSource={connectSource}
          />
        </Map>

        {/* 경로 리스트 패널 (디자인 유지) */}
        <div style={{
          position: 'absolute', top: '100px', left: '20px', zIndex: 10,
          background: 'white', padding: '20px', borderRadius: '18px', 
          boxShadow: '0 8px 30px rgba(0,0,0,0.12)', width: isCollapsed ? '60px' : '320px',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          overflow: 'hidden'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            {!isCollapsed && <h3 style={{ margin: 0, fontSize: '17px', fontWeight: 'bold', color: '#1a1a1a' }}>📍 경로 편집</h3>}
            <button onClick={() => setIsCollapsed(!isCollapsed)} style={{ cursor: 'pointer', border: 'none', background: '#f0f0f0', borderRadius: '50%', width: '30px', height: '30px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              {isCollapsed ? "▶" : "◀"}
            </button>
          </div>

          {!isCollapsed && (
            <div style={{ marginTop: '20px' }}>
              <div style={{ display: 'flex', gap: '8px', marginBottom: '18px' }}>
                <button 
                  onClick={() => {
                    setPath(p => p.slice(0, -1));
                    setConnections(c => c.filter(conn => conn.from < path.length - 1 && conn.to < path.length - 1));
                  }} 
                  style={{ flex: 1, padding: '8px', fontSize: '12px', fontWeight: '600', cursor: 'pointer', borderRadius: '8px', border: '1px solid #eee', background: '#fcfcfc', color: '#ff4d4f' }}
                >
                  ↺ 핀 취소
                </button>
                <button 
                  onClick={() => setConnections(c => c.slice(0, -1))} 
                  style={{ flex: 1, padding: '8px', fontSize: '12px', fontWeight: '600', cursor: 'pointer', borderRadius: '8px', border: '1px solid #eee', background: '#fcfcfc', color: '#1890ff' }}
                >
                  ↩ 선 취소
                </button>
              </div>

              <div style={{ maxHeight: '280px', overflowY: 'auto', paddingRight: '5px' }}>
                {path.map((p, i) => {
                  const isSelected = connectSource === i;
                  return (
                    <div key={i} style={{ display: 'flex', alignItems: 'center', padding: '10px', marginBottom: '6px', borderRadius: '10px', background: isSelected ? '#fff1f0' : '#f9f9f9', border: isSelected ? '1px solid #ffa39e' : '1px solid transparent' }}>
                      <div style={{ width: '24px', fontSize: '11px', fontWeight: 'bold', color: isSelected ? '#cf1322' : '#aaa' }}>{i + 1}</div>
                      {editingIdx === i ? (
                        <input 
                          value={editValue}
                          onChange={(e) => setEditValue(e.target.value)}
                          onBlur={() => saveEditedName(i)}
                          onKeyDown={(e) => e.key === 'Enter' && saveEditedName(i)}
                          autoFocus
                          style={{ flex: 1, padding: '4px 8px', fontSize: '13px', border: '1px solid #4285F4', borderRadius: '6px', outline: 'none' }}
                        />
                      ) : (
                        <div 
                          onClick={() => { setEditingIdx(i); setEditValue(p.name); }}
                          style={{ flex: 1, fontSize: '14px', cursor: 'pointer', fontWeight: isSelected ? '600' : '400', color: isSelected ? '#cf1322' : '#333', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                        >
                          {p.name}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              <button 
                onClick={() => { if(window.confirm("전체 경로를 삭제할까요?")) { setPath([]); setConnections([]); setConnectSource(null); } }} 
                style={{ marginTop: '20px', width: '100%', padding: '12px', borderRadius: '10px', cursor: 'pointer', background: '#141414', color: 'white', border: 'none', fontWeight: 'bold', fontSize: '13px' }}
              >
                전체 초기화
              </button>
            </div>
          )}
        </div>
      </APIProvider>
    </div>
  );
}
