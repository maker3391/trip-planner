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

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tripForm, setTripForm] = useState({
    title: "",
    destination: "",
    startDate: "",
    endDate: "",
  });

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

  const handleOpenModal = () => {
    if(path.length === 0) {
      alert("저장할 경로가 없습니다. 지도에서 장소를 먼저 추가해주세요.")
      return;
    }
    setTripForm(prev => ({...prev, destination: searchKeyword || ""}));
    setIsModalOpen(true);
  };

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const {name, value} = e.target;
    setTripForm(prev => ({...prev, [name]: value}));
  };

  // --- 3. 저장 로직 ---
  const handleSaveToBackend = () => {
    if(!tripForm.title || !tripForm.destination || !tripForm.startDate || !tripForm.endDate) {
      alert("모든 여행 정보를 입력해주세요.");
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
      title: tripForm.title,
      destination: tripForm.destination,
      startDate: tripForm.startDate,
      endDate: tripForm.endDate,
      schedules: schedules
    };
    createTripMutation.mutate(requestData, {
      onSuccess: () => {
      setIsModalOpen(false);
      setTripForm({title: "", destination: "", startDate: "", endDate: ""});
      }
    });
  };

  

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
              onClick={handleOpenModal}
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
      {isModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
          backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 9999,
          display: 'flex', justifyContent: 'center', alignItems: 'center'
        }}>
          <div style={{
            backgroundColor: 'white', padding: '30px', borderRadius: '16px',
            width: '400px', boxShadow: '0 10px 40px rgba(0,0,0,0.2)', display: 'flex', flexDirection: 'column', gap: '15px'
          }}>
            <h2 style={{ margin: '0 0 10px 0', fontSize: '20px' }}>✈️ 여행 정보 입력</h2>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
              <label style={{ fontSize: '12px', fontWeight: 'bold', color: '#666' }}>여행 제목</label>
              <input type="text" name="title" value={tripForm.title} onChange={handleFormChange} placeholder="예: 신나는 부산 먹방 여행" style={{ padding: '10px', borderRadius: '8px', border: '1px solid #ddd' }} />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
              <label style={{ fontSize: '12px', fontWeight: 'bold', color: '#666' }}>목적지 (도시)</label>
              <input type="text" name="destination" value={tripForm.destination} onChange={handleFormChange} placeholder="예: 부산" style={{ padding: '10px', borderRadius: '8px', border: '1px solid #ddd' }} />
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '5px', flex: 1 }}>
                <label style={{ fontSize: '12px', fontWeight: 'bold', color: '#666' }}>시작일</label>
                <input type="date" name="startDate" value={tripForm.startDate} onChange={handleFormChange} style={{ padding: '10px', borderRadius: '8px', border: '1px solid #ddd' }} />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '5px', flex: 1 }}>
                <label style={{ fontSize: '12px', fontWeight: 'bold', color: '#666' }}>종료일</label>
                <input type="date" name="endDate" value={tripForm.endDate} onChange={handleFormChange} style={{ padding: '10px', borderRadius: '8px', border: '1px solid #ddd' }} />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
              <button onClick={() => setIsModalOpen(false)} style={{ flex: 1, padding: '12px', backgroundColor: '#f0f0f0', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold' }}>취소</button>
              <button onClick={handleSaveToBackend} disabled={createTripMutation.isPending} style={{ flex: 1, padding: '12px', backgroundColor: '#4285F4', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold' }}>
                {createTripMutation.isPending ? "저장 중..." : "최종 저장"}
              </button>
            </div>
          </div>
        </div>
      )}
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
