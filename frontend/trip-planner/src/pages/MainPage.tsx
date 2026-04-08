import { useEffect, useState, useCallback } from "react";
import { useLocation } from 'react-router-dom';
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import MyMapApp, { PlacePoint, Connection } from "./MyMapApp"; 
import SaveModal from "../components/trip/SaveModal";
import ActionButtons from "../components/trip/ActionButtons";
import { useCreateTrip, useGetTrip, useUpdateTrip } from "../components/hooks/useTrip";
import { TripPlanRequest, TripPlanResponse } from "../types/trip";
import "./MainPage.css";

export default function MainPage() {
  const location = useLocation();
  const [searchKeyword, setSearchKeyword] = useState("");
  const [path, setPath] = useState<PlacePoint[]>([]);
  const [connections, setConnections] = useState<Connection[]>([]);
  const [targetTripId, setTargetTripId] = useState<number | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tripForm, setTripForm] = useState({ title: "", destination: "", startDate: "", endDate: "" });
  const [calcData, setCalcData] = useState({expenses: [], budget: 0});
  const { data: tripData, isLoading: isTripLoading } = useGetTrip(targetTripId);
  const createTripMutation = useCreateTrip();
  const updateTripMutation = useUpdateTrip(targetTripId);

  // 1. URL ID 체크
  useEffect(() => {
    const state = location.state as { tripId?: number };
    if (state?.tripId) {
      setTargetTripId(state.tripId);
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  // 2. 서버 데이터 로드 시 변환
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
          memo: s.description || ""
        })) || [];

      setPath(recoveredPath);
      setConnections(recoveredPath.map((_, i) => ({ from: i, to: i + 1 })).slice(0, -1));
      setTripForm({ title: tripData.title, destination: tripData.destination, startDate: tripData.startDate, endDate: tripData.endDate });
      window.dispatchEvent(new CustomEvent('LOAD_CALCULATOR_DATA', {
        detail: {
          expenses: tripData.expenses || [],
          budget: tripData.totalBudget || 0,
        }
      }));
    }
  }, [tripData]);

  useEffect(() => {
    const handleCalcSync = (e: any) => {
      setCalcData(e.detail);
    };
    window.addEventListener('SYNC_CALCULATOR', handleCalcSync);
    return () => window.removeEventListener('SYNC_CALCULATOR', handleCalcSync);
  }, []);

  // 3. 저장 로직
  const handleSaveToBackend = () => {
    if (!tripForm.title) return alert("제목을 입력해주세요.");

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

    const expenses = calcData.expenses.map((item: any) => ({
      amount: item.amount,
      category: item.category || 'ETC',
      description: item.description
    }));
    const requestData: any = { 
      ...tripForm,        
      schedules,          
      expenses,           
      totalBudget: calcData.budget
    };

    const mutation = targetTripId ? updateTripMutation : createTripMutation;
    
    mutation.mutate(requestData, {
      onSuccess: (data: any) => {
        if (!targetTripId && data?.id) {
          setTargetTripId(data.id);
        }
        setIsModalOpen(false);
        alert("여행 계획과 예산이 안전하게 저장되었습니다! 💾");
      },
      onError: (error) => {
        console.error("저장 중 오류 발생:", error);
        alert("저장에 실패했습니다. 서버 로그를 확인해주세요.");
      }
    });
  };

  return (
    <div className="main-page">
      <Header />
      <div className="main-page-body" style={{ display: 'flex', height: 'calc(100vh - 60px)' }}>
        <Sidebar onSearch={setSearchKeyword} />
        <main className="map-area" style={{ flexGrow: 1, position: 'relative' }}>
          
          <ActionButtons onOpenSaveModal={() => path.length > 0 ? setIsModalOpen(true) : alert("장소를 추가해주세요.")} isLoading={isTripLoading} />
          
          <div style={{ width: '100%', height: '100%' }}>
            <MyMapApp searchKeyword={searchKeyword} path={path} setPath={setPath} connections={connections} setConnections={setConnections} />
          </div>

        </main>
      </div>

      <SaveModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onSave={handleSaveToBackend} 
        tripForm={tripForm} 
        setTripForm={setTripForm} 
      />
    </div>
  );
}