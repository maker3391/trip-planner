import { useState, useCallback } from 'react';
import { APIProvider, Map } from '@vis.gl/react-google-maps';
import MapController from '../components/map/MapController';
import SideListPanel from '../components/map//SideListPanel';
import DetailPanel from '../components/map//DetailPanel';
import { PlacePoint, Connection } from '../types/map';

export default function MyMapApp({ searchKeyword, path, setPath, connections, setConnections }: any) {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [selectedIdx, setSelectedIdx] = useState<number | null>(null);
  const [pinColor, setPinColor] = useState("#000000");
  const [selectedPinColor, setSelectedPinColor] = useState("#4285F4");
  const [lineColor, setLineColor] = useState("#FF4D4F");
  const [showLines, setShowLines] = useState(true);
  const [showAllMemos, setShowAllMemos] = useState(true);

  // --- 추가된 함수: 불러온 데이터의 사진을 복구하여 path에 저장 ---
  const handlePhotosRestored = useCallback((idx: number, photos: string[]) => {
    setPath((prev: PlacePoint[]) => 
      prev.map((p, i) => i === idx ? { ...p, photos: photos } : p)
    );
  }, [setPath]);

  // 경로 재연결 로직
  const reConnectAll = useCallback((currentPath: PlacePoint[]) => {
    const newConn: Connection[] = [];
    for (let i = 0; i < currentPath.length - 1; i++) {
      newConn.push({ from: i, to: i + 1 });
    }
    setConnections(newConn);
  }, [setConnections]);

  // 지도 클릭 시 포인트 추가
  const handleMapClick = useCallback((newPoint: PlacePoint) => {
    setPath((prev: PlacePoint[]) => {
      const nextPath = [...prev, { ...newPoint }];
      reConnectAll(nextPath);
      setSelectedIdx(nextPath.length - 1);
      return nextPath;
    });
  }, [setPath, reConnectAll]);

  // 핀 삭제 로직
  const handleDeletePin = () => {
    if (selectedIdx === null) return;
    const nextPath = path.filter((_: any, i: number) => i !== selectedIdx);
    setPath(nextPath);
    reConnectAll(nextPath);
    setSelectedIdx(null);
  };

  return (
    <div style={{ width: '100%', height: '100%', display: 'flex', position: 'relative' }}>
      <APIProvider apiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY} libraries={['places']}>
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
              connections={showLines ? connections : []}
              onMapClick={handleMapClick} 
              onSelect={setSelectedIdx} 
              selectedSource={selectedIdx}
              onMemoChange={(idx: number, text: string) => setPath((prev: any) => prev.map((p: any, i: number) => i === idx ? { ...p, memo: text } : p))}
              // --- MapController에 새로 만든 콜백 함수 전달 ---
              onPhotosRestored={handlePhotosRestored} 
              toggleMemo={(idx: number) => setPath((prev: any) => prev.map((p: any, i: number) => i === idx ? { ...p, isMemoOpen: !p.isMemoOpen } : p))}
              lineColor={lineColor} 
              pinColor={pinColor} 
              selectedPinColor={selectedPinColor} 
              showAllMemos={showAllMemos}
            />
          </Map>

          <SideListPanel 
            isCollapsed={isCollapsed}
            setIsCollapsed={setIsCollapsed}
            path={path}
            setPath={setPath}
            selectedIdx={selectedIdx}
            setSelectedIdx={setSelectedIdx}
            pinColor={pinColor}
            setPinColor={setPinColor}
            selectedPinColor={selectedPinColor}
            setSelectedPinColor={setSelectedPinColor}
            lineColor={lineColor}
            setLineColor={setLineColor}
            showLines={showLines}
            setShowLines={setShowLines}
            showAllMemos={showAllMemos}
            setShowAllMemos={setShowAllMemos}
            handleDeletePin={handleDeletePin}
            reConnectAll={reConnectAll}
          />
        </div>

        <DetailPanel 
          selectedIdx={selectedIdx} 
          path={path} 
          setPath={setPath} 
          onClose={() => setSelectedIdx(null)} 
        />
      </APIProvider>
    </div>
  );
}