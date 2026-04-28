import { useEffect, useState, useCallback } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import MyMapApp from "./MyMapApp";
import { PlacePoint, Connection } from "../types/map";
import { SearchPlace } from "../types/searchPlace.ts";
import SaveModal from "../components/trip/SaveModal";
import ActionButtons from "../components/trip/ActionButtons";
import SearchResultModal from "../components/map/SearchResultModal";
import {
  useCreateTrip,
  useGetTrip,
  useUpdateTrip,
} from "../components/hooks/useTrip";

// 계산기 관련 임포트
import Calculator from "../components/layout/Calculator.tsx";

import "./MainPage.css";
import toast from "react-hot-toast";

export default function MainPage() {
  // MainPage.tsx
  const location = useLocation();
  const navigate = useNavigate();
  
  const [searchKeyword, setSearchKeyword] = useState("");
  const [searchResults, setSearchResults] = useState<SearchPlace[]>([]);
  const [selectedSearchPlace, setSelectedSearchPlace] = useState<SearchPlace | null>(null);
  const [isSearchModalOpen, setIsSearchModalOpen] = useState(false);

  const [path, setPath] = useState<PlacePoint[]>([]);
  const [connections, setConnections] = useState<Connection[]>([]);
  const [targetTripId, setTargetTripId] = useState<number | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  const [tripForm, setTripForm] = useState({
    title: "",
    destination: "",
    startDate: "",
    endDate: "",
  });
  const [calcData, setCalcData] = useState<{ expenses: any[]; budget: number }>({
    expenses: [],
    budget: 0,
  });

  const [pinColor, setPinColor] = useState("#000000");
  const [selectedPinColor, setSelectedPinColor] = useState("#4285F4");
  const [lineColor, setLineColor] = useState("#FF4D4F");

  const { data: tripData, isLoading: isTripLoading } = useGetTrip(targetTripId);
  const createTripMutation = useCreateTrip();
  const updateTripMutation = useUpdateTrip(targetTripId);

  // readOnly 상태 읽기
  const isReadOnly = (location.state as { tripId?: number; readOnly?: boolean })?.readOnly ?? false;


  useEffect(() => {
    const state = location.state as { tripId?: number };
    if (state?.tripId) {
      setTargetTripId(state.tripId);
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  useEffect(() => {
    if (tripData && tripData.schedules) {
      if (tripData.schedules.length > 0) {
        const first = tripData.schedules[0];
        if (first.pinColor) setPinColor(first.pinColor);
        if (first.selectedPinColor) setSelectedPinColor(first.selectedPinColor);
        if (first.lineColor) setLineColor(first.lineColor);
      }

      const recoveredPath: PlacePoint[] =
        tripData.schedules
          ?.sort((a: any, b: any) => a.visitOrder - b.visitOrder)
          .map((s: any) => ({
            lat: s.latitude || 0,
            lng: s.longitude || 0,
            name: s.placeName || s.title,
            address: s.placeAddress || "",
            placeId: s.googlePlaceId || undefined,
            customTitle: s.title,
            memo: s.memo || "",
            dayNumber: s.dayNumber ?? 1,
            startTime: s.startTime || "",
            endTime: s.endTime || "",
            estimatedStayMinutes: s.estimatedStayMinutes ?? 60,
          })) || [];

      setPath(recoveredPath);
      setConnections(
        recoveredPath.map((_, i) => ({ from: i, to: i + 1 })).slice(0, -1)
      );

      setTripForm({
        title: tripData.title,
        destination: tripData.destination,
        startDate: tripData.startDate,
        endDate: tripData.endDate,
      });

      window.dispatchEvent(
        new CustomEvent("LOAD_CALCULATOR_DATA", {
          detail: {
            expenses: tripData.expenses || [],
            budget: Number(tripData.totalBudget) || 0,
          },
        })
      );
    }
  }, [tripData]);

  useEffect(() => {
    if (tripData) {
      console.log("tripData.expenses:", JSON.stringify(tripData.expenses, null, 2));
    }
  }, [tripData]);

  useEffect(() => {
    const handleCalcSync = (e: any) => {
      setCalcData({
        expenses: e.detail?.expenses || [],
        budget: Number(e.detail?.budget) || 0,
      });
    };

    window.addEventListener("SYNC_CALCULATOR", handleCalcSync);
    return () => window.removeEventListener("SYNC_CALCULATOR", handleCalcSync);
  }, []);

  const openSearchModal = useCallback(() => {
    setIsSearchModalOpen(true);
  }, []);

  const clearSelectedSearchPlace = useCallback(() => {
    setSelectedSearchPlace(null);
  }, []);

  const closeSearchModal = useCallback(() => {
    setIsSearchModalOpen(false);
    setSearchKeyword("");
    setSearchResults([]);
  }, []);

  const handleSearchResultSelect = useCallback(
    (place: SearchPlace) => {
      setSelectedSearchPlace(place);
      closeSearchModal();
    },
    [closeSearchModal]
  );

  const handleSaveToBackend = () => {
    if (!tripForm.title) {
      return toast.error("제목을 입력해주세요. ✍️", { id: "trip-save-validation" });
    }

    const schedules = path.map((p, index) => ({
      dayNumber: p.dayNumber ?? 1,
      title: p.customTitle || p.name,
      visitOrder: index + 1,
      startTime: p.startTime || null,
      endTime: p.endTime || null,
      estimatedStayMinutes: p.estimatedStayMinutes ?? 60,
      placeName: p.name,
      placeAddress: p.address,
      latitude: p.lat,
      longitude: p.lng,
      googlePlaceId: p.placeId,
      memo: p.memo || "",
      pinColor,
      selectedPinColor,
      lineColor,
    }));

    const expenses = calcData.expenses.map((item: any) => ({
      id: item.id > 0 ? item.id : undefined, // ✅ 양수일 때만 id 전송 (음수는 신규)
      amount: Number(item.amount) || 0,
      category: item.category || "ETC",
      description: item.description || "",
      expenseType: item.expenseType || "ACTUAL",
      subExpenses: (item.subExpenses ?? []).map((sub: any) => ({
        id: sub.id > 0 ? sub.id : undefined, // ✅ 동일
        amount: Number(sub.amount) || 0,
        category: sub.category || "ETC",
        description: sub.description || "",
        expenseType: sub.expenseType || "ACTUAL",
      })),
    }));

    console.log("보내는 expenses:", JSON.stringify(expenses, null, 2));

    const requestData: any = {
      ...tripForm,
      schedules,
      expenses,
      totalBudget: Number(calcData.budget) || 0,
    };

    const mutation = targetTripId ? updateTripMutation : createTripMutation;

    mutation.mutate(requestData, {
      onSuccess: (data: any) => {
        toast.dismiss();
        if (!targetTripId && data?.id) {
          setTargetTripId(data.id);
        }
        setIsModalOpen(false);
        setTimeout(() => navigate("/trip-list"), 0);
      },
      onError: (error) => {
        console.error("저장 중 오류 발생:", error);
        toast.error("저장에 실패했습니다. 다시 시도해주세요.", { id: "trip-save-error" });
      },
    });
  };

  return (
    <div className="main-page">
      <Header />

      <div
        className="main-page-body"
        style={{ display: "flex", height: "calc(100vh - 60px)" }}
      >
        <Sidebar
          onSearch={(keyword) => {
            setSearchKeyword(keyword);
          }}
        />

        <main className="map-area" style={{ flexGrow: 1, position: "relative" }}>
          
          {/* 좌측 하단 플로팅 버튼 컨테이너 */}
          <div style={{
            position: "absolute",
            bottom: "30px",
            left: "20px",
            zIndex: 1001,
          }}>
            <ActionButtons
              onOpenSaveModal={() =>
                path.length > 0
                  ? setIsModalOpen(true)
                  : toast.error("장소를 추가해주세요.", { id: "add-place" })
              }
              isLoading={isTripLoading}
              isReadOnly={isReadOnly}  // ✅ 추가
            />
          </div>
          <div style={{ width: "100%", height: "100%" }}>
            <MyMapApp
              isReadOnly={isReadOnly}
              searchKeyword={searchKeyword}
              setSearchResults={setSearchResults}
              openSearchModal={openSearchModal}
              selectedSearchPlace={selectedSearchPlace}
              clearSelectedSearchPlace={clearSelectedSearchPlace}
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

      <SearchResultModal
        open={isSearchModalOpen}
        keyword={searchKeyword}
        results={searchResults}
        onClose={closeSearchModal}
        onSelect={handleSearchResultSelect}
      />

      <SaveModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveToBackend}
        tripForm={tripForm}
        setTripForm={setTripForm}
      />

      {/* 계산기 컴포넌트: tripId가 null(신규여행)이어도 항상 렌더링되어야 클릭 시 열립니다. */}
      <Calculator readOnly={isReadOnly} /> 
    </div>
  );
}