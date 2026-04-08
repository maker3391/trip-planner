import React, { useEffect, useRef } from 'react';
import {
  useMap,
  useMapsLibrary,
  Marker,
  Polyline,
  InfoWindow
} from '@vis.gl/react-google-maps';
import LocalMemoEditor from './LocalMemoEditor';
import { PlacePoint } from '../../types/map';

interface MapControllerProps {
  searchKeyword: string;
  path: PlacePoint[];
  connections: any[];
  onMapClick: (point: PlacePoint) => void;
  onSelect: (idx: number) => void;
  selectedSource: number | null;
  onMemoChange: (idx: number, text: string) => void;
  onPhotosRestored?: (idx: number, photos: string[]) => void; 
  toggleMemo: (idx: number) => void;
  lineColor: string;
  pinColor: string;
  selectedPinColor: string;
  showAllMemos: boolean;
}

export default function MapController({
  searchKeyword,
  path,
  connections,
  onMapClick,
  onSelect,
  selectedSource,
  onMemoChange,
  onPhotosRestored,
  toggleMemo,
  lineColor,
  pinColor,
  selectedPinColor,
  showAllMemos
}: MapControllerProps) {
  const map = useMap();
  const placesLib = useMapsLibrary('places');
  const lastKeyword = useRef("");

  // --- 1. 검색 키워드 처리 (기본 검색 로직) ---
  useEffect(() => {
    if (!map || !placesLib || !searchKeyword || searchKeyword === lastKeyword.current) return;
    
    const service = new google.maps.places.PlacesService(document.createElement('div'));
    lastKeyword.current = searchKeyword;

    service.findPlaceFromQuery(
      { query: searchKeyword, fields: ['place_id', 'geometry'] },
      (results, status) => {
        if (status === google.maps.places.PlacesServiceStatus.OK && results?.[0]?.geometry?.location) {
          const location = results[0].geometry.location;
          map.setCenter(location);
          map.setZoom(15);

          service.getDetails(
            { 
              placeId: results[0].place_id!, 
              fields: ['name', 'formatted_address', 'photos', 'place_id', 'geometry'], 
              language: 'ko' 
            }, 
            (place, detailStatus) => {
              if (detailStatus === google.maps.places.PlacesServiceStatus.OK && place) {
                onMapClick({
                  lat: Number(place.geometry?.location?.lat() || location.lat()),
                  lng: Number(place.geometry?.location?.lng() || location.lng()),
                  name: place.name || searchKeyword,
                  address: place.formatted_address || "",
                  placeId: place.place_id,
                  photos: place.photos?.map((p: any) => p.getUrl({ maxWidth: 400 })),
                  isMemoOpen: false
                });
              }
            }
          );
        }
      }
    );
  }, [map, placesLib, searchKeyword, onMapClick]);

  // --- 2. 지도 클릭 이벤트 (좌표 및 주소 유실 방지 강화) ---
  useEffect(() => {
    if (!map) return;

    const clickListener = map.addListener('click', (e: google.maps.MapMouseEvent) => {
      const poiEvent = e as google.maps.IconMouseEvent;
      if (poiEvent.placeId) poiEvent.stop(); 

      if (!e.latLng) return;
      
      const currentLat = Number(e.latLng.lat());
      const currentLng = Number(e.latLng.lng());
      const service = new google.maps.places.PlacesService(document.createElement('div'));

      if (poiEvent.placeId) {
        // POI(랜드마크) 클릭 시
        service.getDetails(
          { 
            placeId: poiEvent.placeId, 
            fields: ['name', 'formatted_address', 'photos', 'place_id'], 
            language: 'ko' 
          }, 
          (place, status) => {
            if (status === google.maps.places.PlacesServiceStatus.OK && place) {
              onMapClick({ 
                lat: currentLat, 
                lng: currentLng, 
                name: place.name || "알 수 없는 장소", 
                address: place.formatted_address || "", 
                placeId: place.place_id, 
                photos: place.photos?.map(p => p.getUrl({ maxWidth: 400 })), 
                isMemoOpen: true 
              });
            }
          }
        );
      } else {
        // 일반 길목 클릭 시 역지오코딩 처리
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ location: { lat: currentLat, lng: currentLng }, language: 'ko' }, (results, status) => {
          if (status === google.maps.GeocoderStatus.OK && results?.[0]) {
            const fullAddress = results[0].formatted_address;
            const addressParts = fullAddress.split(' ');
            
            // 주소 뒷부분을 이름으로 추출 (예: '묘봉산로 53-12')
            const displayName = addressParts.length > 2 
              ? `${addressParts[addressParts.length - 2]} ${addressParts[addressParts.length - 1]}` 
              : fullAddress;

            onMapClick({ 
              lat: currentLat, 
              lng: currentLng, 
              name: displayName, 
              address: fullAddress, 
              placeId: results[0].place_id, // 일반 좌표도 place_id가 존재함
              isMemoOpen: true 
            });
          } else {
            onMapClick({ 
              lat: currentLat, 
              lng: currentLng, 
              name: "지정된 위치", 
              address: "상세 주소 정보를 불러올 수 없습니다.", 
              isMemoOpen: true 
            });
          }
        });
      }
    });

    return () => google.maps.event.removeListener(clickListener);
  }, [map, onMapClick]);

  // --- 3. 불러온 데이터 사진 복구 로직 ---
  useEffect(() => {
    if (!map || !placesLib || !path || path.length === 0) return;

    const service = new google.maps.places.PlacesService(document.createElement('div'));

    path.forEach((point, index) => {
      if (point.placeId && (!point.photos || point.photos.length === 0)) {
        service.getDetails(
          { 
            placeId: point.placeId, 
            fields: ['photos'] 
          }, 
          (place, status) => {
            if (status === google.maps.places.PlacesServiceStatus.OK && place?.photos && onPhotosRestored) {
              const restoredPhotos = place.photos.map((p: any) => p.getUrl({ maxWidth: 400 }));
              onPhotosRestored(index, restoredPhotos);
            }
          }
        );
      }
    });
  }, [map, placesLib, path, onPhotosRestored]);

  return (
    <>
      {path.map((point, i) => (
        <React.Fragment key={`${i}-${point.lat}-${point.lng}`}>
          <Marker 
            position={{ lat: point.lat, lng: point.lng }} 
            label={{ text: (i + 1).toString(), color: 'white', fontWeight: 'bold' }}
            icon={{
              path: google.maps.SymbolPath.BACKWARD_CLOSED_ARROW,
              fillColor: selectedSource === i ? selectedPinColor : pinColor,
              fillOpacity: 1, 
              strokeWeight: 2, 
              strokeColor: "#FFFFFF", 
              scale: 8,
            }}
            zIndex={selectedSource === i ? 1000 : i}
            onClick={() => onSelect(i)} 
          />
          {showAllMemos && (
            <InfoWindow 
              position={{ lat: point.lat, lng: point.lng }} 
              headerDisabled={true} 
              pixelOffset={new google.maps.Size(0, -35)}
              disableAutoPan={true}
              shouldFocus={false}
            >
              <div style={{ padding: '8px 5px', width: point.isMemoOpen ? '180px' : 'auto', display: 'flex', flexDirection: 'column' }}>
                <div style={{ fontSize: '12px', fontWeight: 'bold', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '8px' }}>
                  <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{point.name}</span>
                  <button 
                    onClick={(e) => { e.stopPropagation(); toggleMemo(i); }} 
                    style={{ background: '#f0f0f0', border: '1px solid #ccc', borderRadius: '4px', cursor: 'pointer', padding: '1px 4px', fontSize: '10px' }}
                  >
                    {point.isMemoOpen ? '▼' : '▲'}
                  </button>
                </div>
                {point.isMemoOpen && (
                  <LocalMemoEditor 
                    initialMemo={point.memo || ""} 
                    onSave={(val) => onMemoChange(i, val)} 
                  />
                )}
              </div>
            </InfoWindow>
          )}
        </React.Fragment>
      ))}

      {connections.map((conn, i) => (
        path[conn.from] && path[conn.to] && (
          <Polyline 
            key={i} 
            path={[path[conn.from], path[conn.to]]} 
            strokeColor={lineColor} 
            strokeWeight={4} 
            strokeOpacity={0.8} 
          />
        )
      ))}
    </>
  );
}