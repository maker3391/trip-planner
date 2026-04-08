import { useEffect, useState } from "react";
import { useLocation } from 'react-router-dom';
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import MyMapApp from "./MyMapApp"; 
import { PlacePoint, Connection } from "../types/map"; 
import SaveModal from "../components/trip/SaveModal";
import ActionButtons from "../components/trip/ActionButtons";
import { useCreateTrip, useGetTrip, useUpdateTrip } from "../components/hooks/useTrip";
import { TripPlanRequest } from "../types/trip";
import "./MainPage.css";

export default function MainPage() {
  const location = useLocation();
  const [searchKeyword, setSearchKeyword] = useState("");
  const [path, setPath] = useState<PlacePoint[]>([]);
  const [connections, setConnections] = useState<Connection[]>([]);
  const [targetTripId, setTargetTripId] = useState<number | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tripForm, setTripForm] = useState({ title: "", destination: "", startDate: "", endDate: "" });

  // 색상 상태
  const [pinColor, setPinColor] = useState("#000000");
  const [selectedPinColor, setSelectedPinColor] = useState("#4285F4");
  const [lineColor, setLineColor] = useState("#FF4D4F");

  const { data: tripData, isLoading: isTripLoading } = useGetTrip(targetTripId);
  const createTripMutation = useCreateTrip();
  const updateTripMutation = useUpdateTrip(targetTripId);

  useEffect(() => {
    const state = location.state as { tripId?: number };
    if (state?.tripId) {
      setTargetTripId(state.tripId);
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  // 1. 서버 데이터 로드 시 변환 (Memo & Color 복구)
  useEffect(() => {
    if (tripData && tripData.schedules) {
      if (tripData.schedules.length > 0) {
        const first = tripData.schedules[0];
        if (first.pinColor) setPinColor(first.pinColor);
        if (first.selectedPinColor) setSelectedPinColor(first.selectedPinColor);
        if (first.lineColor) setLineColor(first.lineColor);
      }

      const recoveredPath: PlacePoint[] = tripData.schedules
        ?.sort((a: any, b: any) => a.visitOrder - b.visitOrder)
        .map((s: any) => ({
          lat: s.latitude || 0,
          lng: s.longitude || 0,
          name: s.placeName || s.title,
          address: s.placeAddress || "",
          placeId: s.googlePlaceId || undefined,
          customTitle: s.title,
          // 중요: 서버에서 받아온 memo 필드를 프론트 path에 저장
          memo: s.memo || "" 
        })) || [];

      setPath(recoveredPath);
      setConnections(recoveredPath.map((_, i) => ({ from: i, to: i + 1 })).slice(0, -1));
      setTripForm({ 
        title: tripData.title, 
        destination: tripData.destination, 
        startDate: tripData.startDate, 
        endDate: tripData.endDate 
      });
    }
  }, [tripData]);

  // 2. 저장/수정 로직 (Memo & Color 포함)
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
      // 중요: 백엔드 DTO 필드명인 'memo'로 전송
      memo: p.memo, 
      pinColor: pinColor,
      selectedPinColor: selectedPinColor,
      lineColor: lineColor
    }));

    const requestData: TripPlanRequest = { ...tripForm, schedules };

    const mutation = targetTripId ? updateTripMutation : createTripMutation;
    mutation.mutate(requestData, {
      onSuccess: (data: any) => {
        if (!targetTripId && data?.id) setTargetTripId(data.id);
        setIsModalOpen(false);
      }
    });
  };

  return (
    <div className="main-page">
      <Header />
      <div className="main-page-body" style={{ display: 'flex', height: 'calc(100vh - 60px)' }}>
        <Sidebar onSearch={setSearchKeyword} />
        <main className="map-area" style={{ flexGrow: 1, position: 'relative' }}>
          
          <ActionButtons 
            onOpenSaveModal={() => path.length > 0 ? setIsModalOpen(true) : alert("장소를 추가해주세요.")} 
            isLoading={isTripLoading} 
          />
          
          <div style={{ width: '100%', height: '100%' }}>
            <MyMapApp 
              searchKeyword={searchKeyword} 
              path={path} 
              setPath={setPath} 
              connections={connections} 
              setConnections={setConnections}
              pinColor={pinColor}
              setPinColor={setPinColor}
              selectedPinColor={selectedPinColor}
              setSelectedPinColor={setSelectedPinColor}
              lineColor={lineColor}
              setLineColor={setLineColor}
            />
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