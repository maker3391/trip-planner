import React, { useEffect, useState, useCallback } from 'react';
import {
  APIProvider,
  Map,
  useMap,
  useMapsLibrary,
  Marker,
  Polyline,
  InfoWindow // 추가
} from '@vis.gl/react-google-maps';
// 드래그 앤 드롭 추가
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';

// --- 1. 인터페이스 정의 ---
interface PlacePoint {
  lat: number;
  lng: number;
  name: string;
  address: string;
  placeId?: string;
  photos?: string[];

  memo?: string; // 메모 필드 추가
  priceLevel?: number;

}

interface Connection {
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
  // --- 색상 프롭스 ---
  pinColor: string;
  lineColor: string;
  onMemoChange: (idx: number, text: string) => void; // 메모 변경 핸들러 추가
}

// --- 2. 유틸리티 함수 (가격 수준 변환) ---
const getPriceRange = (level?: number) => {
  switch (level) {
    case 0: return "무료";
    case 1: return "₩10,000 미만";
    case 2: return "₩10,000 ~ 20,000";
    case 3: return "₩20,000 ~ 40,000";
    case 4: return "₩40,000 이상";
    default: return null;
  }
};

// --- 3. 지도 컨트롤러 컴포넌트 ---
function MapController({ 
  searchKeyword, path, connections, onMapClick, onConnect, selectedSource, pinColor, lineColor 
}: MapControllerProps) {
  const map = useMap();
  const placesLib = useMapsLibrary('places');

  useEffect(() => {
    if (!map || !placesLib || !searchKeyword.trim()) return;

    const service = new google.maps.places.PlacesService(document.createElement('div'));
    
    service.findPlaceFromQuery({
      query: searchKeyword,
      fields: ['place_id', 'geometry']
    }, (results, status) => {
      if (status === google.maps.places.PlacesServiceStatus.OK && results?.[0]?.place_id) {
        const placeId = results[0].place_id;
        const location = results[0].geometry?.location;

        if (location) {
          map.panTo(location);
          map.setZoom(16);

          service.getDetails({
            placeId: placeId,
            fields: ['name', 'formatted_address', 'photos', 'place_id', 'price_level', 'geometry'],
            language: 'ko'
          }, (place, detailStatus) => {
            if (detailStatus === google.maps.places.PlacesServiceStatus.OK && place) {
              onMapClick({
                lat: place.geometry?.location?.lat() || location.lat(),
                lng: place.geometry?.location?.lng() || location.lng(),
                name: place.name || searchKeyword,
                address: place.formatted_address || "",
                placeId: place.place_id,
                photos: place.photos?.map(p => p.getUrl({ maxWidth: 400 })),
                priceLevel: place.price_level
              });
            }
          });
        }
      }
    });
  }, [map, placesLib, searchKeyword, onMapClick]);

  useEffect(() => {
    if (!map || !placesLib) return;

    const clickListener = map.addListener('click', (e: google.maps.MapMouseEvent | google.maps.IconMouseEvent) => {
      if (!e.latLng) return;
      const lat = e.latLng.lat();
      const lng = e.latLng.lng();
      const service = new google.maps.places.PlacesService(document.createElement('div'));

      if ('placeId' in e && e.placeId) {
        service.getDetails({ 
          placeId: e.placeId, 
          fields: ['name', 'formatted_address', 'photos', 'place_id', 'price_level'], 
          language: 'ko' 
        }, (place, status) => {
          if (status === google.maps.places.PlacesServiceStatus.OK && place) {
            onMapClick({
              lat, lng,
              name: place.name || "장소명 없음",
              address: place.formatted_address || "",
              placeId: place.place_id,
              photos: place.photos?.map(p => p.getUrl({ maxWidth: 400 })),
              priceLevel: place.price_level
            });
          }
        });
        if (e.stop) e.stop();
      } else {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ location: { lat, lng }, language: 'ko' }, (results, status) => {
          if (status === google.maps.GeocoderStatus.OK && results?.[0]) {
            const addr = results[0].formatted_address;
            onMapClick({
              lat, lng,
              name: addr.split(' ').slice(-2).join(' ') || "지정 위치",
              address: addr
            });
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
          key={`${i}-${point.lat}-${point.lng}`} 
          position={{ lat: point.lat, lng: point.lng }} 
          icon={{
            path: google.maps.SymbolPath.BACKWARD_CLOSED_ARROW,
            fillColor: selectedSource === i ? "#000000" : pinColor, // 선택 시 검정색, 평소 사용자 지정색
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

// --- 4. 메인 애플리케이션 컴포넌트 ---
export default function MyMapApp({ searchKeyword }: { searchKeyword: string }) {
  const [path, setPath] = useState<PlacePoint[]>([]);
  const [connections, setConnections] = useState<Connection[]>([]);
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [connectSource, setConnectSource] = useState<number | null>(null);
  
  // --- 색상 관리를 위한 새로운 State 추가 ---
  const [pinColor, setPinColor] = useState("#FF4D4F"); // 기본 핀 색상 (빨강)
  const [lineColor, setLineColor] = useState("#4285F4"); // 기본 선 색상 (파랑)

  const [editingIdx, setEditingIdx] = useState<number | null>(null);
  const [editValue, setEditValue] = useState("");

  const handleMapClick = useCallback((newPoint: PlacePoint) => {
    setPath(prev => {
      const nextPath = [...prev, newPoint];
      setConnectSource(nextPath.length - 1); 
      return nextPath;
    });
  }, []);

  const deleteSelectedPin = () => {
    if (connectSource === null) {
      alert("취소할 핀을 먼저 선택해주세요.");
      return;
    }
    const targetIdx = connectSource;
    setPath(prev => prev.filter((_, i) => i !== targetIdx));
    setConnections(prev => 
      prev
        .filter(conn => conn.from !== targetIdx && conn.to !== targetIdx)
        .map(conn => ({
          from: conn.from > targetIdx ? conn.from - 1 : conn.from,
          to: conn.to > targetIdx ? conn.to - 1 : conn.to
        }))
    );
    setConnectSource(null);
    setEditingIdx(null);
  };

  const handleConnect = (idx: number) => {
    if (idx === -1) { setConnectSource(null); return; }
    if (connectSource === null) {
      setConnectSource(idx);
    } else if (connectSource === idx) {
      setConnectSource(null);
    } else {
      setConnections(prev => [...prev, { from: connectSource, to: idx }]);
      setConnectSource(null);
    }
  };

  const saveName = (idx: number) => {
    if (editValue.trim()) {
      setPath(prev => prev.map((p, i) => i === idx ? { ...p, name: editValue } : p));
    }
    setEditingIdx(null);
  };

  const clearAll = () => {
    if (window.confirm("모든 경로 데이터를 초기화할까요?")) {
      setPath([]);
      setConnections([]);
      setConnectSource(null);
      setEditingIdx(null);
    }
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
              searchKeyword={searchKeyword}
              path={path}
              connections={connections}
              onMapClick={handleMapClick}
              onConnect={handleConnect}
              selectedSource={connectSource}
              // 색상 값 넘겨주기
              pinColor={pinColor}
              lineColor={lineColor}
            />
          </Map>

          {/* 왼쪽 사이드바 패널 */}
          <div style={{
            position: 'absolute', top: '20px', left: '20px', zIndex: 10,
            background: 'white', padding: '20px', borderRadius: '18px', 
            boxShadow: '0 8px 30px rgba(0,0,0,0.12)', width: isCollapsed ? '60px' : '300px',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)', overflow: 'hidden'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: isCollapsed ? 0 : '20px' }}>
              {!isCollapsed && <h3 style={{ margin: 0, fontSize: '17px', fontWeight: 'bold' }}>📍 여행 경로</h3>}
              <button onClick={() => setIsCollapsed(!isCollapsed)} style={{ cursor: 'pointer', border: 'none', background: '#f5f5f5', borderRadius: '50%', width: '30px', height: '30px' }}>
                {isCollapsed ? "▶" : "◀"}
              </button>
            </div>

            {!isCollapsed && (
              <>
                {/* --- 색상 설정 섹션 추가 --- */}
                <div style={{ display: 'flex', gap: '10px', marginBottom: '15px', padding: '12px', background: '#f8f9fa', borderRadius: '12px' }}>
                  <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '5px' }}>
                    <label style={{ fontSize: '11px', fontWeight: 'bold', color: '#666' }}>핀 색상</label>
                    <input 
                      type="color" 
                      value={pinColor} 
                      onChange={(e) => setPinColor(e.target.value)}
                      style={{ width: '28px', height: '28px', border: 'none', cursor: 'pointer', background: 'transparent' }} 
                    />
                  </div>
                  <div style={{ borderLeft: '1px solid #eee' }} />
                  <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '5px' }}>
                    <label style={{ fontSize: '11px', fontWeight: 'bold', color: '#666' }}>선 색상</label>
                    <input 
                      type="color" 
                      value={lineColor} 
                      onChange={(e) => setLineColor(e.target.value)}
                      style={{ width: '28px', height: '28px', border: 'none', cursor: 'pointer', background: 'transparent' }} 
                    />
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '8px', marginBottom: '15px' }}>
                  <button onClick={deleteSelectedPin} style={{ flex: 1, padding: '8px', fontSize: '12px', cursor: 'pointer', borderRadius: '8px', border: '1px solid #eee', color: connectSource !== null ? '#ff4d4f' : '#ccc', background: '#fff', fontWeight: 'bold' }}>핀 취소</button>
                  <button onClick={() => setConnections(c => c.slice(0, -1))} style={{ flex: 1, padding: '8px', fontSize: '12px', cursor: 'pointer', borderRadius: '8px', border: '1px solid #eee', color: '#1890ff', background: '#fff' }}>선 취소</button>
                </div>

                <div style={{ maxHeight: '400px', overflowY: 'auto', paddingRight: '5px' }}>
                  {path.length === 0 && <p style={{ fontSize: '13px', color: '#bbb', textAlign: 'center', padding: '20px 0' }}>지도를 클릭해 보세요!</p>}
                  {path.map((p, i) => {
                    const isSelected = connectSource === i;
                    return (
                      <div key={i} 
                        onClick={() => setConnectSource(i)}
                        style={{ 
                          display: 'flex', alignItems: 'center', padding: '12px', marginBottom: '8px', 
                          borderRadius: '12px', background: isSelected ? '#fff1f0' : '#f9f9f9',
                          border: isSelected ? `1px solid ${pinColor}` : '1px solid transparent',
                          transition: 'all 0.2s', cursor: 'pointer'
                        }}
                      >
                        <span style={{ width: '22px', fontSize: '11px', fontWeight: 'bold', color: isSelected ? pinColor : '#999' }}>{i + 1}</span>
                        {editingIdx === i ? (
                          <input 
                            value={editValue}
                            onChange={(e) => setEditValue(e.target.value)}
                            onBlur={() => saveName(i)}
                            onKeyDown={(e) => e.key === 'Enter' && saveName(i)}
                            autoFocus
                            onClick={(e) => e.stopPropagation()}
                            style={{ flex: 1, padding: '4px 8px', fontSize: '13px', border: `1px solid ${lineColor}`, borderRadius: '6px', outline: 'none' }}
                          />
                        ) : (
                          <div style={{ 
                            flex: 1, fontSize: '14px', 
                            color: isSelected ? '#111' : '#333', 
                            fontWeight: isSelected ? 'bold' : 'normal',
                            overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' 
                          }}>
                            {p.name}
                          </div>
                        )}
                        <button 
                          onClick={(e) => { e.stopPropagation(); setEditingIdx(i); setEditValue(p.name); }}
                          style={{ marginLeft: '8px', border: 'none', background: 'none', cursor: 'pointer', fontSize: '14px', opacity: 0.5 }}
                        >
                          ✏️
                        </button>
                      </div>
                    );
                  })}
                </div>

                <button onClick={clearAll} style={{ marginTop: '20px', width: '100%', padding: '12px', borderRadius: '10px', cursor: 'pointer', background: '#1a1a1a', color: 'white', border: 'none', fontWeight: 'bold', fontSize: '13px' }}>
                  전체 초기화
                </button>
              </>
            )}
          </div>
        </div>

        {/* 오른쪽 상세 정보 패널 */}
        {connectSource !== null && path[connectSource] && (
          <div style={{ width: '380px', background: 'white', boxShadow: '-5px 0 25px rgba(0,0,0,0.1)', zIndex: 11, display: 'flex', flexDirection: 'column', animation: 'slideIn 0.3s ease-out' }}>
            <div style={{ padding: '24px', borderBottom: '1px solid #f0f0f0', position: 'relative' }}>
              <button onClick={() => setConnectSource(null)} style={{ position: 'absolute', right: '20px', top: '20px', border: 'none', background: '#eee', borderRadius: '50%', width: '28px', height: '28px', cursor: 'pointer' }}>×</button>
              <h2 style={{ margin: '0 0 10px 0', fontSize: '20px', fontWeight: 'bold', color: '#111' }}>{path[connectSource].name}</h2>
              
              <div style={{ marginBottom: '15px' }}>
                <a 
                  href={`https://map.naver.com/v5/search/${path[connectSource].name}?c=${path[connectSource].lng},${path[connectSource].lat},15,0,0,0,dh`}
                  target="_blank" 
                  rel="noreferrer"
                  style={{
                    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px',
                    width: 'fit-content', padding: '6px 12px', backgroundColor: '#03C75A', color: 'white',
                    borderRadius: '6px', textDecoration: 'none', fontSize: '12px', fontWeight: 'bold'
                  }}
                >
                  <span>N</span> 네이버 지도에서 정보 확인
                </a>
              </div>
              {/* 추가된 메모가 있으면 상세창에도 표시 */}
              {path[connectSource].memo && (
                <div style={{ marginBottom: '10px', padding: '10px', background: '#f0f7ff', borderRadius: '8px', fontSize: '13px', color: '#0056b3' }}>
                  <strong>메모:</strong> {path[connectSource].memo}
                </div>
              )}
              <p style={{ fontSize: '13px', color: '#666', lineHeight: '1.4', margin: 0 }}>{path[connectSource].address}</p>
            </div>
            
            <div style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
              <h4 style={{ fontSize: '14px', marginBottom: '12px', color: '#333' }}>🖼️ 장소 사진</h4>
              {path[connectSource].photos && path[connectSource].photos!.length > 0 ? (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  {path[connectSource].photos?.slice(0, 8).map((url, i) => (
                    <img key={i} src={url} alt="place detail" style={{ width: '100%', height: '120px', objectFit: 'cover', borderRadius: '12px', border: '1px solid #eee' }} />
                  ))}
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '40px 0', color: '#ccc' }}>
                  <p style={{ fontSize: '13px' }}>등록된 사진이 없습니다.</p>
                </div>
              )}
            </div>
          </div>
        )}
      </APIProvider>

      <style>{`
        @keyframes slideIn {
          from { transform: translateX(100%); opacity: 0; }
          to { transform: translateX(0); opacity: 1; }
        }
        ::-webkit-scrollbar { width: 6px; }
        ::-webkit-scrollbar-thumb { background: #ddd; borderRadius: 10px; }
      `}</style>
    </div>
  );
}