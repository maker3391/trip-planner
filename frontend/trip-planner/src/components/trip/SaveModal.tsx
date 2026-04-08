import React from 'react';
//mainPage에서 모달 컴포넌트로 분리
interface SaveModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: () => void;
  tripForm: { title: string; destination: string; startDate: string; endDate: string };
  setTripForm: React.Dispatch<React.SetStateAction<any>>;
}

export default function SaveModal({ isOpen, onClose, onSave, tripForm, setTripForm }: SaveModalProps) {
  if (!isOpen) return null;

  return (
    <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 9999, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '16px', width: '400px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
        <h2 style={{ margin: 0 }}>✈️ 여행 계획 저장</h2>
        <input 
          type="text" placeholder="여행 제목" 
          value={tripForm.title} 
          onChange={(e) => setTripForm({ ...tripForm, title: e.target.value })} 
          style={{ padding: '12px', borderRadius: '8px', border: '1px solid #ddd' }} 
        />
        <input 
          type="text" placeholder="목적지" 
          value={tripForm.destination} 
          onChange={(e) => setTripForm({ ...tripForm, destination: e.target.value })} 
          style={{ padding: '12px', borderRadius: '8px', border: '1px solid #ddd' }} 
        />
        <div style={{ display: 'flex', gap: '10px' }}>
          <input type="date" value={tripForm.startDate} onChange={(e) => setTripForm({ ...tripForm, startDate: e.target.value })} style={{ flex: 1, padding: '10px' }} />
          <input type="date" value={tripForm.endDate} onChange={(e) => setTripForm({ ...tripForm, endDate: e.target.value })} style={{ flex: 1, padding: '10px' }} />
        </div>
        <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
          <button onClick={onClose} style={{ flex: 1, padding: '12px', borderRadius: '8px', border: 'none', cursor: 'pointer' }}>취소</button>
          <button onClick={onSave} style={{ flex: 1, padding: '12px', borderRadius: '8px', backgroundColor: '#4285F4', color: 'white', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>저장하기</button>
        </div>
      </div>
    </div>
  );
}