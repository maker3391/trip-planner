import React from 'react'; // 리액트 임포트 확인
import { APIProvider, Map } from '@vis.gl/react-google-maps';

export default function MyMapApp() {
  return (
    <APIProvider apiKey="">
      <div style={{ width: '100%', height: '100%', background: 'lightgray' }}> 
        <Map
          style={{ width: '100%', height: '100%' }}
          defaultCenter={{ lat: 37.5665, lng: 126.9780 }}
          defaultZoom={13}
          mapId="88e7468c80808bfd9f3075b0" 
        />
      </div>
    </APIProvider>
  );
}

//apikey는 무조건 삭제AIzaSyA1RObcKLbeR4OkFTIcLZXta4nQElBBsMk