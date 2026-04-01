import React, { useEffect, useState, useCallback } from 'react';
import {
  APIProvider,
  Map,
  useMap,
  useMapsLibrary,
  AdvancedMarker,
  InfoWindow,
  MapMouseEvent,
  Polyline
} from '@vis.gl/react-google-maps';

// --- 인터페이스 정의 ---
interface PlacePoint {
  lat: number;
  lng: number;
  name: string;
}

interface MyMapAppProps {
  searchKeyword: string;
}

// --- 1. 검색 키워드 처리 핸들러 ---
function MapSearchHandler({ searchKeyword }: MyMapAppProps) {
  const map = useMap();
  const geocodingLib = useMapsLibrary('geocoding');

  useEffect(() => {
    if (!map || !geocodingLib || !searchKeyword.trim()) return;

    const geocoder = new geocodingLib.Geocoder();
    geocoder.geocode({ address: searchKeyword }, (results, status) => {
      if (status === 'OK' && results && results[0]) {
        const location = results[0].geometry.location;
        map.panTo(location);
        map.setZoom(15);
      }
    });
  }, [map, geocodingLib, searchKeyword]);

  return null;
}

// --- 2. 경로 안내 및 직선 그리기 ---
function Directions({ path }: { path: PlacePoint[] }) {
  const map = useMap();
  const routesLib = useMapsLibrary('routes');
  const [directions, setDirections] = useState<google.maps.DirectionsResult | null>(null);

  useEffect(() => {
    if (!map || !routesLib || path.length < 2) {
      setDirections(null);
      return;
    }

    const service = new google.maps.DirectionsService();
    service.route({
      origin: { lat: path[0].lat, lng: path[0].lng },
      destination: { lat: path[path.length - 1].lat, lng: path[path.length - 1].lng },
      waypoints: path.slice(1, -1).map(p => ({
        location: { lat: p.lat, lng: p.lng },
        stopover: true
      })),
      travelMode: google.maps.TravelMode.TRANSIT,
    }, (result, status) => {
      if (status === google.maps.DirectionsStatus.OK && result) {
        setDirections(result);
      } else {
        setDirections(null);
        console.warn("도로 경로를 찾을 수 없어 직선으로 표시합니다.");
      }
    });
  }, [path, map, routesLib]);

  if (directions) {
    return <DirectionsRendererResult directions={directions} />;
  }

  return (
    <Polyline
      path={path}
      strokeColor="#FF4500"
      strokeWeight={4}
      strokeOpacity={0.8}
    />
  );
}

// DirectionsRenderer를 위한 내부 컴포넌트
function DirectionsRendererResult({ directions }: { directions: google.maps.DirectionsResult }) {
  const map = useMap();
  const [renderer, setRenderer] = useState<google.maps.DirectionsRenderer | null>(null);

  useEffect(() => {
    if (!map) return;
    const dr = new google.maps.DirectionsRenderer({ map, suppressMarkers: true });
    setRenderer(dr);
    return () => dr.setMap(null);
  }, [map]);

  useEffect(() => {
    if (renderer) renderer.setDirections(directions);
  }, [renderer, directions]);

  return null;
}

// --- 3. 메인 컴포넌트 ---
export default function MyMapApp({ searchKeyword }: MyMapAppProps) {
  const [path, setPath] = useState<PlacePoint[]>([]);
  const [selectedIdx, setSelectedIdx] = useState<number | null>(null);
  
  const map = useMap();
  const placesLib = useMapsLibrary('places');

  const handleMapClick = useCallback((e: MapMouseEvent) => {
    // e.detail.latLng가 null일 수 있으므로 체크
    if (!e.detail.latLng || !map || !placesLib) return;
    
    const { lat, lng } = e.detail.latLng;
    const service = new google.maps.places.PlacesService(map);

    service.nearbySearch({ location: { lat, lng }, radius: 50 }, (results, status) => {
      const name = (status === google.maps.places.PlacesServiceStatus.OK && results && results[0]?.name) 
        ? results[0].name 
        : "좌표 지점";
      
      setPath(prev => [...prev, { lat, lng, name }]);
    });
  }, [map, placesLib]);

  return (
    <div style={{ width: '100%', height: '100%', overflow: 'hidden', position: 'relative' }}>
      <APIProvider apiKey="">
        <Map
          style={{ width: '100%', height: '100%' }}
          defaultCenter={{ lat: 37.5665, lng: 126.9780 }}
          defaultZoom={13}
          mapId=""
          onClick={handleMapClick}
          disableDefaultUI={true}
        >
          <MapSearchHandler searchKeyword={searchKeyword} />

          <Directions path={path} />

          {path.map((point, i) => (
            <React.Fragment key={`${i}-${point.lat}-${point.lng}`}>
              <AdvancedMarker 
                position={{ lat: point.lat, lng: point.lng }} 
                onClick={() => setSelectedIdx(i)} 
              />
              {selectedIdx === i && (
                <InfoWindow 
                  position={{ lat: point.lat, lng: point.lng }} 
                  onCloseClick={() => setSelectedIdx(null)}
                >
                  <div style={{ color: 'black', padding: '5px' }}>
                    <strong>{point.name}</strong>
                  </div>
                </InfoWindow>
              )}
            </React.Fragment>
          ))}
        </Map>

        {/* 경로 관리 UI */}
        <div style={{
          position: 'absolute', top: '15px', left: '15px',
          background: 'white', padding: '15px', borderRadius: '8px',
          boxShadow: '0 2px 10px rgba(0,0,0,0.1)', zIndex: 10, width: '200px'
        }}>
          <button 
            onClick={() => {
              setPath([]);
              setSelectedIdx(null);
            }}
            style={{ 
              width: '100%', padding: '8px', backgroundColor: '#ff4d4f', 
              color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' 
            }}
          >
            경로 초기화
          </button>
          <div style={{ marginTop: '10px', fontSize: '12px', maxHeight: '150px', overflowY: 'auto' }}>
            {path.map((p, i) => (
              <div key={i} style={{ padding: '4px 0', borderBottom: '1px solid #eee' }}>
                {i + 1}. {p.name}
              </div>
            ))}
          </div>
        </div>
      </APIProvider>
    </div>
  );
}

//apikey는 무조건 삭제AIzaSyA1RObcKLbeR4OkFTIcLZXta4nQElBBsMk
// map 88e7468c80808bfd9f3075b0
