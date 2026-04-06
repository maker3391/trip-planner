import React, { useEffect, useState, useCallback } from 'react';
import {
  APIProvider,
  Map,
  useMap,
  useMapsLibrary,
  Marker,
  Polyline
} from '@vis.gl/react-google-maps';

// --- 1. 인터페이스 정의 (export를 붙여야 MainPage에서 읽을 수 있습니다) ---
export interface PlacePoint {
  lat: number;
  lng: number;
  name: string;
  address: string;
  customTitle?: string;
  placeId?: string;
  photos?: string[];
}

export interface Connection {
  from: number;
  to: number;
}

interface MapControllerProps {
  searchKeyword: string;
  path: PlacePoint[];
  connections: Connection[];
  onMapClick: (point: PlacePoint) => void;
  onConnect: (idx: number) => void;
  selectedSource: number | null;
  pinColor: string;
  selectedPinColor: string;
  lineColor: string;
}

interface MyMapAppProps {
  searchKeyword: string;
  path: PlacePoint[];
  setPath: React.Dispatch<React.SetStateAction<PlacePoint[]>>;
  connections: Connection[];
  setConnections: React.Dispatch<React.SetStateAction<Connection[]>>;
}

// --- 2. 지도 컨트롤러 컴포넌트 (파일 내부에 위치) ---
function MapController({ 
  searchKeyword, path, connections, onMapClick, onConnect, selectedSource, 
  pinColor, selectedPinColor, lineColor 
}: MapControllerProps) {
  const map = useMap();
  const placesLib = useMapsLibrary('places');

  useEffect(() => {
    if (!map || !placesLib || !searchKeyword.trim()) return;
    const service = new google.maps.places.PlacesService(document.createElement('div'));
    service.findPlaceFromQuery({ query: searchKeyword, fields: ['place_id', 'geometry'] }, (results, status) => {
      if (status === google.maps.places.PlacesServiceStatus.OK && results?.[0]?.geometry?.location) {
        const location = results[0].geometry.location;
        map.panTo(location);
        map.setZoom(16);
        service.getDetails({ placeId: results[0].place_id!, fields: ['name', 'formatted_address', 'photos', 'place_id', 'geometry'], language: 'ko' }, (place, detailStatus) => {
          if (detailStatus === google.maps.places.PlacesServiceStatus.OK && place) {
            onMapClick({
              lat: place.geometry?.location?.lat() || location.lat(),
              lng: place.geometry?.location?.lng() || location.lng(),
              name: place.name || searchKeyword,
              address: place.formatted_address || "",
              placeId: place.place_id,
              photos: place.photos?.map(p => p.getUrl({ maxWidth: 400 })),
            });
          }
        });
      }
    });
  }, [map, placesLib, searchKeyword, onMapClick]);

  useEffect(() => {
    if (!map) return;
    const clickListener = map.addListener('click', (e: google.maps.MapMouseEvent) => {
      const poiEvent = e as google.maps.IconMouseEvent;
      if (!e.latLng) return;
      const lat = e.latLng.lat();
      const lng = e.latLng.lng();
      if (poiEvent.placeId) {
        const service = new google.maps.places.PlacesService(document.createElement('div'));
        service.getDetails({ placeId: poiEvent.placeId, fields: ['name', 'formatted_address', 'photos', 'place_id'], language: 'ko' }, (place, status) => {
          if (status === google.maps.places.PlacesServiceStatus.OK && place) {
            onMapClick({ lat, lng, name: place.name || "장소", address: place.formatted_address || "", placeId: place.place_id, photos: place.photos?.map(p => p.getUrl({ maxWidth: 400 })) });
          }
        });
        if (e.stop) e.stop();
      } else {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ location: { lat, lng }, language: 'ko' }, (results, status) => {
          if (status === google.maps.GeocoderStatus.OK && results?.[0]) {
            onMapClick({ lat, lng, name: results[0].formatted_address.split(' ').slice(-2).join(' '), address: results[0].formatted_address, placeId: results[0].place_id });
          }
        });
      }
    });
    return () => google.maps.event.removeListener(clickListener);
  }, [map, onMapClick]);

  return (
    <>
      {path.map((point, i) => (
        <Marker 
          key={`${i}-${point.lat}`} 
          position={{ lat: point.lat, lng: point.lng }} 
          icon={{
            path: google.maps.SymbolPath.BACKWARD_CLOSED_ARROW,
            fillColor: selectedSource === i ? selectedPinColor : pinColor,
            fillOpacity: 1,
            strokeWeight: 2,
            strokeColor: "#FFFFFF",
            scale: 8,
          }}
          onClick={() => onConnect(i)}
        />
      ))}
      {connections.map((conn, i) => (
        <Polyline 
          key={i} 
          path={[path[conn.from], path[conn.to]]} 
          strokeColor={lineColor} 
          strokeWeight={4} 
          strokeOpacity={0.7} 
        />
      ))}
    </>
  );
}

// --- 3. 메인 애플리케이션 컴포넌트 ---
export default function MyMapApp({ 
  searchKeyword, path, setPath, connections, setConnections 
}: MyMapAppProps) {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [connectSource, setConnectSource] = useState<number | null>(null);
  const [pinColor, setPinColor] = useState("#000000");
  const [selectedPinColor, setSelectedPinColor] = useState("#4285F4");
  const [lineColor, setLineColor] = useState("#FF4D4F");
  const [editingIdx, setEditingIdx] = useState<number | null>(null);
  const [editValue, setEditValue] = useState("");

  const handleMapClick = useCallback((newPoint: PlacePoint) => {
    setPath(prev => {
      const nextPath = [...prev, newPoint];
      setConnectSource(nextPath.length - 1); 
      return nextPath;
    });
  }, [setPath]);

  const handleConnect = (idx: number) => {
    if (connectSource === null) {
      setConnectSource(idx);
    } else if (connectSource === idx) {
      setConnectSource(null);
    } else {
      const isAlreadyConnected = connections.some(conn => 
        (conn.from === connectSource && conn.to === idx) || 
        (conn.from === idx && conn.to === connectSource)
      );
      if (isAlreadyConnected) {
        alert("이미 연결된 경로입니다.");
        setConnectSource(null);
        return;
      }
      setConnections(prev => [...prev, { from: connectSource, to: idx }]);
      setConnectSource(null);
    }
  };

  const deleteSelectedPin = () => {
    if (connectSource === null) return alert("핀을 선택해주세요.");
    const targetIdx = connectSource;
    setPath(prev => prev.filter((_, i) => i !== targetIdx));
    setConnections(prev => prev
      .filter(conn => conn.from !== targetIdx && conn.to !== targetIdx)
      .map(conn => ({
        from: conn.from > targetIdx ? conn.from - 1 : conn.from,
        to: conn.to > targetIdx ? conn.to - 1 : conn.to
      }))
    );
    setConnectSource(null);
    setEditingIdx(null);
  };

  const saveName = (idx: number) => {
    if (editValue.trim()) {
      setPath(prev => prev.map((p, i) => i === idx ? { ...p, name: editValue } : p));
    }
    setEditingIdx(null);
  };

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative', display: 'flex', fontFamily: 'sans-serif' }}>
      <APIProvider apiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY} libraries={['places', 'geocoding']}>
        <div style={{ flex: 1, position: 'relative' }}>
          <Map
            style={{ width: '100%', height: '100%' }}
            defaultCenter={{ lat: 35.179, lng: 129.075 }}
            defaultZoom={13}
            mapId={import.meta.env.VITE_GOOGLE_MAP_ID}
            disableDefaultUI={true}
          >
            <MapController 
              searchKeyword={searchKeyword} path={path} connections={connections}
              onMapClick={handleMapClick} onConnect={handleConnect} selectedSource={connectSource}
              pinColor={pinColor} selectedPinColor={selectedPinColor} lineColor={lineColor}
            />
          </Map>

          {/* 왼쪽 사이드바 */}
          <div style={{
            position: 'absolute', top: '20px', left: '20px', zIndex: 10,
            background: 'white', padding: '20px', borderRadius: '18px', 
            boxShadow: '0 8px 30px rgba(0,0,0,0.12)', width: isCollapsed ? '60px' : '300px',
            transition: 'all 0.3s', overflow: 'hidden'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: isCollapsed ? 0 : '15px' }}>
              {!isCollapsed && <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 'bold' }}>📍 여행 경로</h3>}
              <button onClick={() => setIsCollapsed(!isCollapsed)} style={{ cursor: 'pointer', border: 'none', background: '#f5f5f5', borderRadius: '50%', width: '28px', height: '28px' }}>
                {isCollapsed ? "▶" : "◀"}
              </button>
            </div>

            {!isCollapsed && (
              <>
                <div style={{ display: 'flex', gap: '5px', marginBottom: '15px', padding: '10px', background: '#f8f9fa', borderRadius: '12px', justifyContent: 'space-between' }}>
                  <ColorPickerItem label="기본 핀" value={pinColor} onChange={setPinColor} />
                  <ColorPickerItem label="선택 핀" value={selectedPinColor} onChange={setSelectedPinColor} />
                  <ColorPickerItem label="경로 선" value={lineColor} onChange={setLineColor} />
                </div>

                <div style={{ display: 'flex', gap: '8px', marginBottom: '15px' }}>
                  <button onClick={deleteSelectedPin} style={{ flex: 1, padding: '8px', fontSize: '12px', cursor: 'pointer', borderRadius: '8px', border: '1px solid #eee', color: connectSource !== null ? '#ff4d4f' : '#ccc', background: '#fff', fontWeight: 'bold' }}>핀 취소</button>
                  <button onClick={() => setConnections(c => c.slice(0, -1))} style={{ flex: 1, padding: '8px', fontSize: '12px', cursor: 'pointer', borderRadius: '8px', border: '1px solid #eee', color: '#1890ff', background: '#fff' }}>선 취소</button>
                </div>

                <div style={{ maxHeight: '350px', overflowY: 'auto', paddingRight: '5px' }}>
                  {path.map((p, i) => (
                    <div key={i} onClick={() => setConnectSource(i)} style={{ 
                      display: 'flex', alignItems: 'center', padding: '10px', marginBottom: '6px', borderRadius: '10px', 
                      background: connectSource === i ? '#fff1f0' : '#f9f9f9', cursor: 'pointer', border: connectSource === i ? `1px solid ${selectedPinColor}` : '1px solid transparent'
                    }}>
                      <span style={{ width: '20px', fontSize: '11px', fontWeight: 'bold', color: connectSource === i ? selectedPinColor : '#999' }}>{i + 1}</span>
                      {editingIdx === i ? (
                        <input value={editValue} onChange={(e) => setEditValue(e.target.value)} onBlur={() => saveName(i)} onKeyDown={(e) => e.key === 'Enter' && saveName(i)} autoFocus style={{ flex: 1, fontSize: '13px' }} />
                      ) : (
                        <div style={{ flex: 1, fontSize: '13px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.name}</div>
                      )}
                      <button onClick={(e) => { e.stopPropagation(); setEditingIdx(i); setEditValue(p.name); }} style={{ border: 'none', background: 'none', cursor: 'pointer', opacity: 0.5 }}>✏️</button>
                    </div>
                  ))}
                </div>
                <button onClick={() => { if(window.confirm("초기화할까요?")) { setPath([]); setConnections([]); setConnectSource(null); }}} style={{ marginTop: '15px', width: '100%', padding: '10px', borderRadius: '8px', cursor: 'pointer', background: '#1a1a1a', color: 'white', border: 'none', fontSize: '12px' }}>전체 초기화</button>
              </>
            )}
          </div>
        </div>

        {/* 오른쪽 상세 패널 */}
        {connectSource !== null && path[connectSource] && (
          <div style={{ width: '380px', background: 'white', boxShadow: '-5px 0 25px rgba(0,0,0,0.1)', zIndex: 11, display: 'flex', flexDirection: 'column', animation: 'slideIn 0.3s ease-out' }}>
            <div style={{ padding: '24px', borderBottom: '1px solid #f0f0f0', position: 'relative' }}>
              <button onClick={() => setConnectSource(null)} style={{ position: 'absolute', right: '20px', top: '20px', border: 'none', background: '#eee', borderRadius: '50%', width: '28px', height: '28px', cursor: 'pointer' }}>×</button>
              <h2 style={{ margin: '0 0 10px 0', fontSize: '20px', fontWeight: 'bold' }}>{path[connectSource].name}</h2>
              <p style={{ fontSize: '13px', color: '#666', margin: 0 }}>{path[connectSource].address}</p>
            </div>
            <div style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
              {path[connectSource].photos && path[connectSource].photos!.length > 0 ? (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                  {path[connectSource].photos?.map((url, i) => (
                    <img key={i} src={url} style={{ width: '100%', height: '100px', objectFit: 'cover', borderRadius: '8px' }} alt="place" />
                  ))}
                </div>
              ) : <p style={{ color: '#ccc', fontSize: '12px' }}>사진이 없습니다.</p>}
            </div>
          </div>
        )}
      </APIProvider>
      <style>{`@keyframes slideIn { from { transform: translateX(100%); } to { transform: translateX(0); } }`}</style>
    </div>
  );
}

function ColorPickerItem({ label, value, onChange }: { label: string, value: string, onChange: (v: string) => void }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '3px' }}>
      <label style={{ fontSize: '10px', fontWeight: 'bold', color: '#888' }}>{label}</label>
      <input type="color" value={value} onChange={(e) => onChange(e.target.value)} style={{ width: '24px', height: '24px', border: 'none', cursor: 'pointer', background: 'transparent' }} />
    </div>
  );
}