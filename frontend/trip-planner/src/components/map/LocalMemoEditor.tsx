import { useEffect, useState } from "react";

interface LocalMemoEditorProps {
  initialMemo: string;
  onSave: (val: string) => void;
}

export default function LocalMemoEditor({ initialMemo, onSave }: LocalMemoEditorProps) {
  // 사용자가 입력 중인 임시 상태
  const [tempMemo, setTempMemo] = useState(initialMemo);

  // 외부(서버나 다른 선택)에서 메모가 바뀌면 동기화
  useEffect(() => {
    setTempMemo(initialMemo);
  }, [initialMemo]);

  return (
    <textarea 
      value={tempMemo} 
      onChange={(e) => setTempMemo(e.target.value)}
      // 입력을 마치고 포커스가 나갈 때(Blur) 부모 상태에 저장
      onBlur={() => onSave(tempMemo)}
      // 중요: 텍스트 입력 중 지도의 단축키나 이벤트가 발생하는 것 방지
      onKeyDown={(e) => e.stopPropagation()} 
      placeholder="메모를 입력하세요..."
      style={{ 
        border: '1px solid #e0e0e0', 
        padding: '12px', 
        fontSize: '13px', 
        width: '100%', 
        height: '100px', 
        outline: 'none', 
        resize: 'none', 
        boxSizing: 'border-box', 
        borderRadius: '8px', 
        marginTop: '8px', 
        backgroundColor: '#fafafa', 
        fontFamily: 'inherit',
        lineHeight: '1.5'
      }}
    />
  );
}