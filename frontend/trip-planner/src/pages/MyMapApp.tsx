import React, { useEffect, useState, useCallback } from 'react';
import {
  APIProvider,
  Map,
  useMap,
  useMapsLibrary,
  Marker,
  Polyline,
  InfoWindow
} from '@vis.gl/react-google-maps';
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';

// --- 인터페이스 ---
export interface PlacePoint {
  lat: number;
  lng: number;
  name: string;
  address: string;
  placeId?: string;
  photos?: string[];
  memo?: string;
  isMemoOpen?: boolean;
}

// --- 메모 포커스 튐 방지 컴포넌트 ---
function LocalMemoEditor({ initialMemo, onSave }: { initialMemo: string, onSave: (val: string) => void }) {
  const [tempMemo, setTempMemo] = useState(initialMemo);
  useEffect(() => { setTempMemo(initialMemo); }, [initialMemo]);

  return (
    <textarea 
      value={tempMemo} 
      onChange={(e) => setTempMemo(e.target.value)}
      onBlur={() => onSave(tempMemo)}
      onKeyDown={(e) => e.stopPropagation()} 
      placeholder="메모 입력..."
      style={{ 
        border: '1px solid #e0e0e0', padding: '8px', fontSize: '12px', 
        width: '100%', height: '70px', outline: 'none', resize: 'none', 
        boxSizing: 'border-box', borderRadius: '6px', marginTop: '8px', 
        backgroundColor: '#fafafa', fontFamily: 'inherit'
      }}
    />
  );
}

// --- 지도 컨트롤러 (클릭 로직 복구) ---
function MapController({ 
  searchKeyword, path, connections, onMapClick, onSelect, selectedSource, 
  onMemoChange, toggleMemo, lineColor, pinColor, selectedPinColor, showAllMemos
}: any) {
  const map = useMap();
  const placesLib = useMapsLibrary('places');

  // 검색 로직
  useEffect(() => {
    if (!map || !placesLib || !searchKeyword.trim()) return;
    const service = new google.maps.places.PlacesService(document.createElement('div'));
    service.findPlaceFromQuery({ query: searchKeyword, fields: ['place_id', 'geometry'] }, (results, status) => {
      if (status === google.maps.places.PlacesServiceStatus.OK && results?.[0]?.geometry?.location) {
        const location = results[0].geometry.location;
        map.panTo(location);
        service.getDetails({ placeId: results[0].place_id!, fields: ['name', 'formatted_address', 'photos', 'place_id', 'geometry'], language: 'ko' }, (place, detailStatus) => {
          if (detailStatus === google.maps.places.PlacesServiceStatus.OK && place) {
            onMapClick({
              lat: place.geometry?.location?.lat() || location.lat(),
              lng: place.geometry?.location?.lng() || location.lng(),
              name: place.name || searchKeyword,
              address: place.formatted_address || "",
              placeId: place.place_id,
              photos: place.photos?.map((p: any) => p.getUrl({ maxWidth: 400 })),
              isMemoOpen: true
            });
          }
        });
      }
    });
  }, [map, placesLib, searchKeyword]);

  // 클릭 로직 복구 (지점 정보 받아오기)
  useEffect(() => {
    if (!map) return;
    const clickListener = map.addListener('click', (e: google.maps.MapMouseEvent) => {
      const poiEvent = e as google.maps.IconMouseEvent;
      if (!e.latLng) return;
      const lat = e.latLng.lat();
      const lng = e.latLng.lng();

      if (poiEvent.placeId) {
        const service = new google.maps.places.PlacesService(document.createElement('div'));
        service.getDetails({ placeId: poiEvent.placeId, fields: ['name', 'formatted_address', 'photos'], language: 'ko' }, (place, status) => {
          if (status === google.maps.places.PlacesServiceStatus.OK && place) {
            onMapClick({ lat, lng, name: place.name!, address: place.formatted_address!, photos: place.photos?.map(p => p.getUrl({maxWidth:400})), isMemoOpen: true });
          }
        });
      } else {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ location: { lat, lng }, language: 'ko' }, (results, status) => {
          if (status === google.maps.GeocoderStatus.OK && results?.[0]) {
            const addr = results[0].formatted_address;
            onMapClick({ lat, lng, name: addr.split(' ').slice(-2).join(' '), address: addr, isMemoOpen: true });
          }
        });
      }
    });
    return () => google.maps.event.removeListener(clickListener);
  }, [map, onMapClick]);

  return (
    <>
      {path.map((point: PlacePoint, i: number) => (
        <React.Fragment key={`${i}-${point.lat}-${point.lng}`}>
          <Marker 
            position={{ lat: point.lat, lng: point.lng }} 
            label={{ text: (i+1).toString(), color: 'white', fontWeight: 'bold' }}
            icon={{
              path: google.maps.SymbolPath.BACKWARD_CLOSED_ARROW,
              fillColor: selectedSource === i ? selectedPinColor : pinColor,
              fillOpacity: 1, strokeWeight: 2, strokeColor: "#FFFFFF", scale: 8,
            }}
            zIndex={selectedSource === i ? 1000 : i}
            onClick={() => onSelect(i)} 
          />
          {showAllMemos && (
            <InfoWindow position={{ lat: point.lat, lng: point.lng }} headerDisabled={true} pixelOffset={new google.maps.Size(0, -35)}>
              <div style={{ padding: '8px 5px', width: point.isMemoOpen ? '180px' : 'auto', display: 'flex', flexDirection: 'column' }}>
                <div style={{ fontSize: '12px', fontWeight: 'bold', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{point.name}</span>
                  <button onClick={(e) => { e.stopPropagation(); toggleMemo(i); }} style={{ background: '#f0f0f0', border: '1px solid #ccc', borderRadius: '4px', cursor: 'pointer', padding: '1px 4px', fontSize: '10px' }}>
                    {point.isMemoOpen ? '▲' : '▼'}
                  </button>
                </div>
                {point.isMemoOpen && <LocalMemoEditor initialMemo={point.memo || ""} onSave={(val) => onMemoChange(i, val)} />}
              </div>
            </InfoWindow>
          )}
        </React.Fragment>
      ))}
      {connections.map((conn: any, i: number) => (
        path[conn.from] && path[conn.to] && <Polyline key={i} path={[path[conn.from], path[conn.to]]} strokeColor={lineColor} strokeWeight={4} strokeOpacity={0.8} />
      ))}
    </>
  );
}

// --- 메인 앱 ---
export default function MyMapApp({ searchKeyword, path, setPath, connections, setConnections }: any) {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [selectedIdx, setSelectedIdx] = useState<number | null>(null);
  const [pinColor, setPinColor] = useState("#000000");
  const [selectedPinColor, setSelectedPinColor] = useState("#4285F4");
  const [lineColor, setLineColor] = useState("#FF4D4F");
  const [showLines, setShowLines] = useState(true);
  const [showAllMemos, setShowAllMemos] = useState(true);

  const reConnectAll = (currentPath: PlacePoint[]) => {
    const newConn: any[] = [];
    for (let i = 0; i < currentPath.length - 1; i++) newConn.push({ from: i, to: i + 1 });
    setConnections(newConn);
  };

  const handleMapClick = useCallback((newPoint: PlacePoint) => {
    setPath((prev: PlacePoint[]) => {
      const nextPath = [...prev, { ...newPoint, isMemoOpen: true }];
      reConnectAll(nextPath);
      setSelectedIdx(nextPath.length - 1);
      return nextPath;
    });
  }, []);

  const openNaverMap = (point: PlacePoint) => {
    const url = `https://map.naver.com/v5/search/${encodeURIComponent(point.name)}/place?c=${point.lng},${point.lat},15,0,0,0,dh`;
    window.open(url, '_blank');
  };

  return (
    <div style={{ width: '100%', height: '100vh', display: 'flex', fontFamily: 'sans-serif' }}>
      <APIProvider apiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY} libraries={['places']}>
        <div style={{ flex: 1, position: 'relative' }}>
          <Map style={{ width: '100%', height: '100%' }} defaultCenter={{ lat: 35.179, lng: 129.075 }} defaultZoom={13} mapId={import.meta.env.VITE_GOOGLE_MAP_ID} disableDefaultUI={true}>
            <MapController 
              searchKeyword={searchKeyword} path={path} connections={showLines ? connections : []}
              onMapClick={handleMapClick} onSelect={setSelectedIdx} selectedSource={selectedIdx}
              onMemoChange={(idx: number, text: string) => setPath((prev: any) => prev.map((p: any, i: number) => i === idx ? { ...p, memo: text } : p))}
              toggleMemo={(idx: number) => setPath((prev: any) => prev.map((p: any, i: number) => i === idx ? { ...p, isMemoOpen: !p.isMemoOpen } : p))}
              lineColor={lineColor} pinColor={pinColor} selectedPinColor={selectedPinColor} showAllMemos={showAllMemos}
            />
          </Map>

          {/* 왼쪽 패널 */}
          <div style={{
            position: 'absolute', top: '20px', left: '20px', zIndex: 10, background: 'white', padding: '24px', 
            borderRadius: '20px', boxShadow: '0 10px 40px rgba(0,0,0,0.1)', width: isCollapsed ? '60px' : '340px', transition: '0.3s'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: isCollapsed ? 0 : '20px' }}>
              {!isCollapsed && <h3 style={{ margin: 0, fontSize: '18px' }}>📍 여행 경로</h3>}
              <button onClick={() => setIsCollapsed(!isCollapsed)} style={{ cursor: 'pointer', border: 'none', background: '#f5f5f5', borderRadius: '50%', width: '30px', height: '30px' }}>{isCollapsed ? "▶" : "◀"}</button>
            </div>

            {!isCollapsed && (
              <>
                <div style={{ background: '#fcfcfc', border: '1px solid #f0f0f0', borderRadius: '12px', padding: '15px', marginBottom: '15px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px', gap: '10px' }}>
                    <ColorPicker label="기본 핀 색상" value={pinColor} onChange={setPinColor} />
                    <ColorPicker label="선택된 핀 색상" value={selectedPinColor} onChange={setSelectedPinColor} />
                    <ColorPicker label="경로 선 색상" value={lineColor} onChange={setLineColor} />
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button onClick={() => setShowLines(!showLines)} style={{ flex: 1, padding: '8px', fontSize: '11px', cursor: 'pointer', background: '#fff', border: '1px solid #ddd', borderRadius: '6px' }}>{showLines ? "선 숨기기" : "선 보이기"}</button>
                    <button onClick={() => setShowAllMemos(!showAllMemos)} style={{ flex: 1, padding: '8px', fontSize: '11px', cursor: 'pointer', background: '#fff', border: '1px solid #ddd', borderRadius: '6px' }}>{showAllMemos ? "메모 숨기기" : "메모 보이기"}</button>
                  </div>
                </div>
                
                <button 
                  onClick={() => { if(selectedIdx !== null) { const newP = path.filter((_: any, i: number) => i !== selectedIdx); setPath(newP); reConnectAll(newP); setSelectedIdx(null); } }}
                  disabled={selectedIdx === null}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', marginBottom: '10px', background: selectedIdx !== null ? '#fff2f0' : '#f5f5f5', color: selectedIdx !== null ? '#ff4d4f' : '#ccc', border: '1px solid #ffa39e', fontSize: '13px', fontWeight: 'bold', cursor: selectedIdx !== null ? 'pointer' : 'default' }}
                >선택된 핀 삭제</button>

                <DragDropContext onDragEnd={(result) => {
                  if (!result.destination) return;
                  const newPath = Array.from(path);
                  const [reorderedItem] = newPath.splice(result.source.index, 1);
                  newPath.splice(result.destination.index, 0, reorderedItem);
                  setPath(newPath); reConnectAll(newPath); setSelectedIdx(result.destination.index);
                }}>
                  <Droppable droppableId="pathList">
                    {(provided) => (
                      <div {...provided.droppableProps} ref={provided.innerRef} style={{ maxHeight: '300px', overflowY: 'auto' }}>
                        {path.map((p: any, i: number) => (
                          <Draggable key={`${i}-${p.lat}`} draggableId={`${i}-${p.lat}`} index={i}>
                            {(provided) => (
                              <div ref={provided.innerRef} {...provided.draggableProps} {...provided.dragHandleProps} onClick={() => setSelectedIdx(i)} style={{ ...provided.draggableProps.style, display: 'flex', alignItems: 'center', padding: '12px', marginBottom: '8px', borderRadius: '12px', background: selectedIdx === i ? '#fff1f0' : '#f9f9f9', border: selectedIdx === i ? '1px solid #ff4d4f' : '1px solid transparent' }}>
                                <span style={{ width: '25px', fontSize: '12px', fontWeight: 'bold', color: selectedIdx === i ? '#ff4d4f' : '#999' }}>{i + 1}</span>
                                <div style={{ flex: 1, fontSize: '13px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{p.name}</div>
                              </div>
                            )}
                          </Draggable>
                        ))}
                        {provided.placeholder}
                      </div>
                    )}
                  </Droppable>
                </DragDropContext>
                <button onClick={() => { if(confirm("초기화?")) { setPath([]); setConnections([]); setSelectedIdx(null); }}} style={{ marginTop: '15px', width: '100%', padding: '12px', borderRadius: '10px', background: '#333', color: '#fff', border: 'none', fontSize: '13px', cursor: 'pointer' }}>전체 초기화</button>
              </>
            )}
          </div>
        </div>

        {/* 오른쪽 상세 패널 (네이버 지도 연결 추가) */}
        {selectedIdx !== null && path[selectedIdx] && (
          <div style={{ width: '360px', background: 'white', boxShadow: '-5px 0 20px rgba(0,0,0,0.05)', zIndex: 11, display: 'flex', flexDirection: 'column' }}>
            <div style={{ padding: '24px', borderBottom: '1px solid #f0f0f0', position: 'relative' }}>
              <button onClick={() => setSelectedIdx(null)} style={{ position: 'absolute', right: '20px', top: '20px', border: 'none', background: '#f0f0f0', borderRadius: '50%', cursor: 'pointer', width: '25px', height: '25px' }}>×</button>
              <h3 style={{ margin: '0 0 8px 0', fontSize: '20px' }}>{path[selectedIdx].name}</h3>
              <p style={{ fontSize: '13px', color: '#666', marginBottom: '15px' }}>{path[selectedIdx].address}</p>
              <button 
                onClick={() => openNaverMap(path[selectedIdx!])}
                style={{ width: '100%', padding: '10px', borderRadius: '8px', background: '#03C75A', color: 'white', border: 'none', fontSize: '13px', fontWeight: 'bold', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
              >
                N 네이버 지도로 보기
              </button>
            </div>
            <div style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
              {path[selectedIdx].photos && path[selectedIdx].photos.length > 0 ? (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                  {path[selectedIdx].photos.map((url: string, i: number) => <img key={i} src={url} style={{ width: '100%', height: '110px', objectFit: 'cover', borderRadius: '8px' }} alt="place" />)}
                </div>
              ) : <p style={{ fontSize: '12px', color: '#ccc', textAlign: 'center' }}>사진 정보가 없습니다.</p>}
            </div>
          </div>
        )}
      </APIProvider>
    </div>
  );
}

function ColorPicker({ label, value, onChange }: any) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '5px', flex: 1 }}>
      <label style={{ fontSize: '10px', color: '#888', fontWeight: 'bold', textAlign: 'center' }}>{label}</label>
      <input type="color" value={value} onChange={(e) => onChange(e.target.value)} style={{ border: 'none', width: '30px', height: '25px', cursor: 'pointer', background: 'transparent' }} />
    </div>
  );
}