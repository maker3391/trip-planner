import { useEffect, useState } from "react";
import { useLocation } from 'react-router-dom';
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import GuidePopup from "../components/guide/GuidePopup";
import MyMapApp, { PlacePoint, Connection } from "./MyMapApp"; 
import { useCreateTrip, useGetTrip } from "../components/hooks/useTrip"; // useGetTrip 추가
import { TripPlanRequest, TripPlanResponse } from "../types/trip";
import "./MainPage.css";
import { useUpdateTrip } from "../components/hooks/useTrip";


export default function MainPage() {
  const location = useLocation();
  const [openGuidePopup, setOpenGuidePopup] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  
  // 지도에 그릴 상태
  const [path, setPath] = useState<PlacePoint[]>([]);
  const [connections, setConnections] = useState<Connection[]>([]);
  
  // 현재 로드할 대상 ID (초기값은 null, 전달받으면 ID가 들어감)
  const [targetTripId, setTargetTripId] = useState<number | null>(null);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tripForm, setTripForm] = useState({
    title: "",
    destination: "",
    startDate: "",
    endDate: "",
  });

  const createTripMutation = useCreateTrip();

  // --- 1. 특정 ID의 데이터를 서버에서 가져오는 React Query 훅 ---
  // targetTripId가 바뀔 때마다 자동으로 실행되어 데이터를 가져옵니다.
  const { data: tripData, isLoading: isTripLoading } = useGetTrip(targetTripId);

  // --- 2. 페이지 진입 시 URL state로 전달된 ID가 있는지 체크 ---
  useEffect(() => {
    const state = location.state as { tripId?: number };
    if (state?.tripId) {
      console.log("전달받은 Trip ID:", state.tripId);
      setTargetTripId(state.tripId); // ID를 설정하는 순간 useGetTrip이 작동함
      
      // 처리 후 state 초기화 (새로고침 시 중복 로딩 방지)
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  // --- 3. 서버에서 데이터(tripData)가 도착하면 지도 형식으로 변환 ---
  useEffect(() => {
    if (tripData) {
      const recoveredPath: PlacePoint[] = tripData.schedules
        ?.sort((a, b) => a.visitOrder - b.visitOrder)
        .map(s => ({
          lat: s.latitude || 0,
          lng: s.longitude || 0,
          name: s.placeName || s.title,
          address: s.placeAddress || "",
          placeId: s.googlePlaceId || undefined,
          customTitle: s.title,
          memo: s.description || "" // 저장했던 메모 복원
        })) || [];

      setPath(recoveredPath);

      const recoveredConnections: Connection[] = [];
      for (let i = 0; i < recoveredPath.length - 1; i++) {
        recoveredConnections.push({ from: i, to: i + 1 });
      }
      setConnections(recoveredConnections);
      
      // 폼 정보도 불러온 데이터로 세팅 (수정 저장 시 편리함)
      setTripForm({
        title: tripData.title,
        destination: tripData.destination,
        startDate: tripData.startDate,
        endDate: tripData.endDate,
      });
    }
  }, [tripData]);

  // 저장 로직 (생략 없이 포함)
  const handleOpenModal = () => {
    if(path.length === 0) {
      alert("지도에 장소를 먼저 추가해주세요.");
      return;
    }
    setIsModalOpen(true);
  };

  const updateTripMutation = useUpdateTrip(targetTripId);

  const handleSaveToBackend = () => {
    if(!tripForm.title) return alert("제목을 입력해주세요.");

    const schedules = path.map((p, index) => ({
      dayNumber: 1,
      title: p.customTitle || p.name,
      visitOrder: index + 1,
      estimatedStayMinutes: 60,
      placeName: p.name,
      placeAddress: p.address,
      latitude: p.lat,
      longitude: p.lng,
      googlePlaceId: p.placeId,
      description: p.memo
    }));

    const requestData: TripPlanRequest = { ...tripForm, schedules };

    if (targetTripId) {
      // [수정 모드] PATCH 호출
      updateTripMutation.mutate(requestData, {
        onSuccess: () => setIsModalOpen(false)
      });
    } else {
      // [신규 생성 모드] POST 호출
      createTripMutation.mutate(requestData, {
        onSuccess: (data: TripPlanResponse) => {
          // [중요] 방금 생성된 계획의 ID를 상태에 저장함
          // 이렇게 하면 다음 저장 시 자동으로 '수정' 모드가 됩니다.
          if (data && data.id) {
            setTargetTripId(data.id);
          }
          setIsModalOpen(false);
        }
      });
    }
  };

  return (
    <div className="main-page">
      <Header />
      <div className="main-page-body" style={{ display: 'flex', height: 'calc(100vh - 60px)' }}>
        <Sidebar onSearch={setSearchKeyword} />
        <main className="map-area" style={{ flexGrow: 1, position: 'relative' }}>
          
          {/* 하단 버튼들 */}
          <div style={{ position: 'absolute', bottom: '30px', left: '30px', zIndex: 100, display: 'flex', gap: '12px' }}>
            <button
              onClick={handleOpenModal}
              style={{ padding: '14px 28px', backgroundColor: '#1a1a1a', color: 'white', border: 'none', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer', boxShadow: '0 4px 15px rgba(0,0,0,0.2)' }}
            >
              <span>💾</span> 계획 저장
            </button>

            {/* 버튼: 특정 ID를 직접 불러오고 싶을 때 사용
            <button
              onClick={() => setTargetTripId(3)} // 클릭 시 3번 계획 즉시 서버 호출
              style={{ padding: '14px 28px', backgroundColor: '#4285F4', color: 'white', border: 'none', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer', boxShadow: '0 4px 15px rgba(0,0,0,0.2)' }}
            >
              <span>📂</span> 3번 계획 바로 불러오기
            </button>
             */}
            {isTripLoading && <span style={{alignSelf:'center', fontSize:'12px'}}>데이터 로딩 중...</span>}
          </div>

          <div style={{ width: '100%', height: '100%' }}>
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

      {/* 저장 모달 (기존 동일) */}
      {isModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 9999, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '16px', width: '400px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
            <h2 style={{margin:0}}>✈️ 여행 계획 저장</h2>
            <input type="text" placeholder="여행 제목" value={tripForm.title} onChange={(e)=>setTripForm({...tripForm, title: e.target.value})} style={{padding:'12px', borderRadius:'8px', border:'1px solid #ddd'}}/>
            <input type="text" placeholder="목적지" value={tripForm.destination} onChange={(e)=>setTripForm({...tripForm, destination: e.target.value})} style={{padding:'12px', borderRadius:'8px', border:'1px solid #ddd'}}/>
            <div style={{display:'flex', gap:'10px'}}>
              <input type="date" value={tripForm.startDate} onChange={(e)=>setTripForm({...tripForm, startDate: e.target.value})} style={{flex:1, padding:'10px'}}/>
              <input type="date" value={tripForm.endDate} onChange={(e)=>setTripForm({...tripForm, endDate: e.target.value})} style={{flex:1, padding:'10px'}}/>
            </div>
            <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
              <button onClick={() => setIsModalOpen(false)} style={{flex:1, padding:'12px', borderRadius:'8px', border:'none', cursor:'pointer'}}>취소</button>
              <button onClick={handleSaveToBackend} style={{flex:1, padding:'12px', borderRadius:'8px', backgroundColor:'#4285F4', color:'white', border:'none', cursor:'pointer', fontWeight:'bold'}}>저장하기</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}