import { useEffect, useState } from "react";

interface LocalMemoEditorProps {
  initialMemo: string;
  onSave: (val: string) => void;
  isReadOnly?: boolean;
}

export default function LocalMemoEditor({ initialMemo, onSave, isReadOnly = false }: LocalMemoEditorProps) {
  // 사용자가 입력 중인 임시 상태
  const [tempMemo, setTempMemo] = useState(initialMemo);

  // 외부(서버나 다른 선택)에서 메모가 바뀌면 동기화
  useEffect(() => {
    setTempMemo(initialMemo);
  }, [initialMemo]);

  return (
    <textarea 
      value={tempMemo} 
      onChange={(e) => !isReadOnly && setTempMemo(e.target.value)}  // ✅
      onBlur={() => !isReadOnly && onSave(tempMemo)}  // ✅
      onKeyDown={(e) => e.stopPropagation()} 
      readOnly={isReadOnly}  // ✅
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
        backgroundColor: isReadOnly ? '#f5f5f5' : '#fafafa',  // ✅ 시각적 표시
        cursor: isReadOnly ? 'not-allowed' : 'text',  // ✅
        fontFamily: 'inherit',
        lineHeight: '1.5'
      }}
    />
  );
}