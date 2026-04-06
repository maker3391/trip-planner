import { useEffect, useState } from "react";
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import GuidePopup from "../components/guide/GuidePopup";
import MyMapApp, { PlacePoint, Connection } from "./MyMapApp"; // MyMapApp에서 타입 가져오기
import { useCreateTrip } from "../components/hooks/useTrip";
import { TripPlanRequest } from "../types/trip";
import "./MainPage.css";

export default function MainPage() {
  const [openGuidePopup, setOpenGuidePopup] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");

  // --- 1. 공유 상태 관리 (Lifting State Up) ---
  const [path, setPath] = useState<PlacePoint[]>([]);
  const [connections, setConnections] = useState<Connection[]>([]);

  const createTripMutation = useCreateTrip();

  // --- 2. ID 생성 및 저장 로직 ---

  // 위경도를 조합해 고유 ID 생성 (예: "35.123456_129.123456")
  // const getPointId = (p: PlacePoint) =>
  //   `${p.lat.toFixed(6)}_${p.lng.toFixed(6)}`;


  // * 기존 handleSaveToBackend가 Backend 구조와 맞지 않아 새로 작성 예정 기존 구조는 주석으로 남겨두겠습니다.
  // const handleSaveToBackend = async () => {
  //   if (path.length === 0) {
  //     alert("저장할 경로가 없습니다!");
  //     return;
  //   }
  //   // 백엔드 DTO 규격에 맞게 데이터 가공
  //   const requestData = {
  //     // 핀(노드) 목록
  //     nodes: path.map(p => ({
  //       id: getPointId(p), // 위경도 기반 고유 ID
  //       name: p.name,
  //       lat: p.lat,
  //       lng: p.lng,
  //       address: p.address
  //     })),
  //     // 연결(엣지) 목록
  //     edges: connections.map(conn => ({
  //       fromId: getPointId(path[conn.from]),
  //       toId: getPointId(path[conn.to])
  //     }))
  //   };

  //   try {
  //     // 실제 백엔드 API 주소로 변경 필요
  //     const response = await fetch('http://localhost:8080/api/travel/save', {
  //       method: 'POST',
  //       headers: { 'Content-Type': 'application/json' },
  //       body: JSON.stringify(requestData),
  //     });

  //     if (response.ok) {
  //       alert("✅ 여행 경로가 안전하게 저장되었습니다!");
  //     } else {
  //       const errorData = await response.json();
  //       alert(`❌ 저장 실패: ${errorData.message || '서버 오류'}`);
  //     }
  //   } catch (error) {
  //     console.error("전송 중 오류 발생:", error);
  //     alert("서버와 연결할 수 없습니다. 백엔드 서버가 켜져 있는지 확인하세요.");
  //   }
  // };
  // * 새로 작성한 handleSaveToBackend
  const handleSaveToBackend = () => {
    if(path.length === 0) {
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

    const requestData : TripPlanRequest = {
      title: searchKeyword ? `${searchKeyword} 여행 일정` : "나의 여행 계획",
      destination: "부산",
      startDate: "2026-05-01",
      endDate: "2026-05-03",
      schedules: schedules
    };
    createTripMutation.mutate(requestData);
  }

  // 가이드 팝업 로직
  useEffect(() => {
    const today = new Date().toISOString().split("T")[0];
    const hiddenDate = localStorage.getItem("hideGuidePopupDate");

    if (hiddenDate !== today) {
      setOpenGuidePopup(true);
    }
  }, []);

  return (
    <div className="main-page">
      <Header />
      
      <div className="main-page-body" style={{ display: 'flex', height: 'calc(100vh - 60px)' }}>
        <Sidebar onSearch={setSearchKeyword} />
        
        <main className="map-area" style={{ flexGrow: 1, position: 'relative' }}>
          
          {/* --- 하단 버튼 그룹 --- */}
          <div style={{
            position: 'absolute', bottom: '30px', left: '30px',
            zIndex: 100, display: 'flex', gap: '12px'
          }}>
            {/* 저장 버튼 */}
            <button
              onClick={handleSaveToBackend}
              style={{
                padding: '14px 28px', backgroundColor: '#1a1a1a', color: 'white',
                border: 'none', borderRadius: '12px', fontWeight: 'bold',
                boxShadow: '0 4px 20px rgba(0,0,0,0.3)', cursor: 'pointer',
                display: 'flex', alignItems: 'center', gap: '10px'
              }}
            >
              <span>💾</span> 저장하기
            </button>

            {/* 불러오기 테스트 버튼 (필요 시 제거) */}
            <button
              onClick={() => fetchTripExample(1)} // 임의의 ID 1번 로드 예시
              style={{
                padding: '14px 28px', backgroundColor: '#4285F4', color: 'white',
                border: 'none', borderRadius: '12px', fontWeight: 'bold',
                boxShadow: '0 4px 20px rgba(0,0,0,0.3)', cursor: 'pointer',
                display: 'flex', alignItems: 'center', gap: '10px'
              }}
            >
              <span>📂</span> 불러오기
            </button>
          </div>

          <div className="map-placeholder" style={{ width: '100%', height: '100%' }}>
            {/* 상태와 변경 함수를 자식(MyMapApp)에게 전달 */}
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

      <GuidePopup
        open={openGuidePopup}
        onClose={() => setOpenGuidePopup(false)}
      />
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
