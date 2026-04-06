import { useEffect, useState } from "react";
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import GuidePopup from "../components/guide/GuidePopup";
import MyMapApp, { PlacePoint, Connection } from "./MyMapApp"; 
import { useCreateTrip, useGetTrip } from "../components/hooks/useTrip"; // useGetTrip 추가
import { TripPlanRequest, TripPlanResponse } from "../types/trip";
import "./MainPage.css";

export default function MainPage() {
  const [openGuidePopup, setOpenGuidePopup] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [path, setPath] = useState<PlacePoint[]>([]);
  const [connections, setConnections] = useState<Connection[]>([]);
  
  // 불러올 여행 ID를 관리하는 상태 (테스트용으로 1번 설정 가능)
  const [targetTripId, setTargetTripId] = useState<number | null>(null);

  const createTripMutation = useCreateTrip();
  
  // --- 1. 데이터 불러오기 훅 사용 ---
  const { data: tripData } = useGetTrip(targetTripId);

  // --- 2. 불러온 데이터를 지도 형식으로 변환하는 함수 ---
  const handleLoadTripData = (data: TripPlanResponse) => {
    if (!data.schedules || data.schedules.length === 0) return;

    const recoveredPath: PlacePoint[] = data.schedules
      .sort((a, b) => a.visitOrder - b.visitOrder)
      .map(s => ({
        lat: s.latitude || 0,
        lng: s.longitude || 0,
        name: s.placeName || s.title,
        address: s.placeAddress || "",
        placeId: s.googlePlaceId || undefined,
        customTitle: s.title,
      }));

    setPath(recoveredPath);

    const recoveredConnections: Connection[] = [];
    for (let i = 0; i < recoveredPath.length - 1; i++) {
      recoveredConnections.push({ from: i, to: i + 1 });
    }
    setConnections(recoveredConnections);
  };

  // tripData가 변경될 때마다(데이터가 들어올 때마다) 지도 업데이트
  useEffect(() => {
    if (tripData) {
      handleLoadTripData(tripData);
    }
  }, [tripData]);

  // --- 3. 저장 로직 ---
  const handleSaveToBackend = () => {
    if (path.length === 0) {
      alert("저장할 경로가 없습니다!");
      return;
    }

    const schedules = path.map((p, index) => ({
      dayNumber: 1,
      title: p.customTitle || p.name,
      visitOrder: index + 1,
      estimatedStayMinutes: 60,
      placeName: p.name,
      placeAddress: p.address,
      latitude: p.lat,
      longitude: p.lng,
      googlePlaceId: p.placeId
    }));

    const requestData: TripPlanRequest = {
      title: searchKeyword ? `${searchKeyword} 여행 일정` : "나의 여행 계획",
      destination: "부산",
      startDate: "2026-05-01",
      endDate: "2026-05-03",
      schedules: schedules
    };
    createTripMutation.mutate(requestData);
  }

  useEffect(() => {
    const today = new Date().toISOString().split("T")[0];
    const hiddenDate = localStorage.getItem("hideGuidePopupDate");
    if (hiddenDate !== today) setOpenGuidePopup(true);
  }, []);

  return (
    <div className="main-page">
      <Header />
      <div className="main-page-body" style={{ display: 'flex', height: 'calc(100vh - 60px)' }}>
        <Sidebar onSearch={setSearchKeyword} />
        <main className="map-area" style={{ flexGrow: 1, position: 'relative' }}>
          
          <div style={{ position: 'absolute', bottom: '30px', left: '30px', zIndex: 100, display: 'flex', gap: '12px' }}>
            <button
              onClick={handleSaveToBackend}
              style={{ padding: '14px 28px', backgroundColor: '#1a1a1a', color: 'white', border: 'none', borderRadius: '12px', fontWeight: 'bold', boxShadow: '0 4px 20px rgba(0,0,0,0.3)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '10px' }}
            >
              <span>💾</span> 저장하기
            </button>

            {/* 버튼을 누르면 targetTripId를 변경하여 useGetTrip이 작동하게 함 */}
            <button
              onClick={() => setTargetTripId(1)} // 테스트용 1번 ID 호출
              style={{ padding: '14px 28px', backgroundColor: '#4285F4', color: 'white', border: 'none', borderRadius: '12px', fontWeight: 'bold', boxShadow: '0 4px 20px rgba(0,0,0,0.3)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '10px' }}
            >
              <span>📂</span> 1번 계획 불러오기
            </button>
          </div>

          <div className="map-placeholder" style={{ width: '100%', height: '100%' }}>
            <MyMapApp 
              searchKeyword={searchKeyword}
              path={path}
              setPath={setPath}
              connections={connections}
              setConnections={setConnections}
            />
          </div>
        </main>
      </div>
      <GuidePopup open={openGuidePopup} onClose={() => setOpenGuidePopup(false)} />
    </div>
  );
}

// // 인터페이스 정의 (상위에서 관리)
// 지도는 특수한 인터페이스라 그냥 지도에서 바로 가져오기로함
// export interface PlacePoint {
//   lat: number;
//   lng: number;
//   name: string;
//   address: string;
//   placeId?: string;
//   photos?: string[];
// }

// export interface Connection {
//   from: number;
//   to: number;
// }
