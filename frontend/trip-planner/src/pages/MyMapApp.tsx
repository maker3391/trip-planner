import { useState, useCallback } from "react";
import { APIProvider, Map } from "@vis.gl/react-google-maps";
import MapController from "../components/map/MapController";
import SideListPanel from "../components/map/SideListPanel";
import DetailPanel from "../components/map/DetailPanel";
import { PlacePoint, Connection } from "../types/map";
import { SearchPlace } from "../types/searchPlace.ts";

interface MyMapAppProps {
  searchKeyword: string;
  setSearchResults: React.Dispatch<React.SetStateAction<SearchPlace[]>>;
  openSearchModal: () => void;
  selectedSearchPlace: SearchPlace | null;
  clearSelectedSearchPlace: () => void;
  path: PlacePoint[];
  setPath: any;
  connections: Connection[];
  setConnections: any;
  pinColor: string;
  setPinColor: (color: string) => void;
  selectedPinColor: string;
  setSelectedPinColor: (color: string) => void;
  lineColor: string;
  setLineColor: (color: string) => void;
  isReadOnly?: boolean;
}

export default function MyMapApp({
  searchKeyword,
  setSearchResults,
  openSearchModal,
  selectedSearchPlace,
  clearSelectedSearchPlace,
  path,
  setPath,
  connections,
  setConnections,
  pinColor,
  setPinColor,
  selectedPinColor,
  setSelectedPinColor,
  lineColor,
  setLineColor,
  isReadOnly = false,
}: MyMapAppProps) {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [selectedIdx, setSelectedIdx] = useState<number | null>(null);
  const [showLines, setShowLines] = useState(true);
  const [showAllMemos, setShowAllMemos] = useState(true);

  const handlePhotosRestored = useCallback(
    (idx: number, photos: string[]) => {
      setPath((prev: PlacePoint[]) =>
        prev.map((p, i) => (i === idx ? { ...p, photos } : p))
      );
    },
    [setPath]
  );

  const reConnectAll = useCallback(
    (currentPath: PlacePoint[]) => {
      const newConn: Connection[] = [];
      for (let i = 0; i < currentPath.length - 1; i++) {
        newConn.push({ from: i, to: i + 1 });
      }
      setConnections(newConn);
    },
    [setConnections]
  );

  const handleMapClick = useCallback(
    (newPoint: PlacePoint) => {
      if (isReadOnly) return;
      setPath((prev: PlacePoint[]) => {
        const nextPath = [...prev, { ...newPoint }];
        reConnectAll(nextPath);
        setSelectedIdx(nextPath.length - 1);
        return nextPath;
      });
    },
    [setPath, reConnectAll, isReadOnly]
  );

  const handleDeletePin = () => {
    if (isReadOnly) return;
    if (selectedIdx === null) return;
    const nextPath = path.filter((_: any, i: number) => i !== selectedIdx);
    setPath(nextPath);
    reConnectAll(nextPath);
    setSelectedIdx(null);
  };

  return (
    <div style={{ width: "100%", height: "100%", display: "flex", position: "relative" }}>
      <APIProvider
        apiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY}
        libraries={["places"]}
      >
        <div style={{ flex: 1, position: "relative" }}>
          <Map
            style={{ width: "100%", height: "100%" }}
            defaultCenter={{ lat: 35.179, lng: 129.075 }}
            defaultZoom={13}
            mapId={import.meta.env.VITE_GOOGLE_MAP_ID}
            disableDefaultUI={true}
          >
            <MapController
              searchKeyword={searchKeyword}
              setSearchResults={setSearchResults}
              openSearchModal={openSearchModal}
              selectedSearchPlace={selectedSearchPlace}
              clearSelectedSearchPlace={clearSelectedSearchPlace}
              path={path}
              connections={showLines ? connections : []}
              onMapClick={handleMapClick}
              onSelect={setSelectedIdx}
              selectedSource={selectedIdx}
              onMemoChange={(idx: number, text: string) => {
                if (isReadOnly) return;  // ✅ 메모 수정 차단
                setPath((prev: any) =>
                  prev.map((p: any, i: number) =>
                    i === idx ? { ...p, memo: text } : p
                  )
                );
              }}
              onPhotosRestored={handlePhotosRestored}
              toggleMemo={(idx: number) =>
                setPath((prev: any) =>
                  prev.map((p: any, i: number) =>
                    i === idx ? { ...p, isMemoOpen: !p.isMemoOpen } : p
                  )
                )
              }
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
            isReadOnly={isReadOnly}
          />
        </div>

        <DetailPanel
          selectedIdx={selectedIdx}
          path={path}
          setPath={setPath}
          onClose={() => setSelectedIdx(null)}
          isReadOnly={isReadOnly}
        />
      </APIProvider>
    </div>
  );
}