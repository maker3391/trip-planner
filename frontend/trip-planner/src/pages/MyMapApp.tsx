import React, { useEffect, useState } from 'react'; // 리액트 임포트 확인
import { AdvancedMarker, APIProvider, Map, useMap, useMapsLibrary } from '@vis.gl/react-google-maps';

interface MyMapAppProps {
  searchKeyword: string;
}

interface SearchResult {
  lat: number;
  lng: number;
}

function MapSearchHandler({searchKeyword}: {searchKeyword: string}) {
  const map = useMap();
  const geocodingLibrary = useMapsLibrary("geocoding");
  const [markerPosition, setMarkerPosition] = useState<SearchResult | null>(null);

  useEffect(() => {
    if (!map || !geocodingLibrary || !searchKeyword.trim()) return;

    const geocoder = new geocodingLibrary.Geocoder();

    geocoder.geocode({address: searchKeyword}, (results, status) => {
      if (status === "OK" && results && results.length > 0) {
        const location = results[0].geometry.location;
        const lat = location.lat();
        const lng = location.lng();

        const newPosition = {lat, lng};

        map.panTo(newPosition);
        map.setZoom(15);
        setMarkerPosition(newPosition);
      } else {
        alert("검색 결과를 찾을 수 없습니다.");
      }
    });
  }, [map, geocodingLibrary, searchKeyword]);

  return markerPosition ? <AdvancedMarker position={markerPosition} /> : null;
}

export default function MyMapApp({searchKeyword}: MyMapAppProps) {
  return (
    <APIProvider apiKey="AIzaSyA1RObcKLbeR4OkFTIcLZXta4nQElBBsMk">
      <div style={{ width: '100%', height: '100%', background: 'lightgray' }}> 
        <Map
          style={{ width: '100%', height: '100%' }}
          defaultCenter={{ lat: 37.5665, lng: 126.9780 }}
          defaultZoom={13}
          mapId="88e7468c80808bfd9f3075b0" 
        >
          <MapSearchHandler searchKeyword={searchKeyword} />
        </Map>
      </div>
    </APIProvider>
  );
}

//apikey는 무조건 삭제AIzaSyA1RObcKLbeR4OkFTIcLZXta4nQElBBsMk