import LocalMemoEditor from "./LocalMemoEditor";

export default function DetailPanel({
  selectedIdx,
  path,
  setPath,
  onClose,
  isReadOnly = false,
}: any) {
  if (selectedIdx === null || !path[selectedIdx]) return null;
  const point = path[selectedIdx];

  const updateSelectedPoint = (field: string, value: string | number) => {
    setPath((prev: any[]) =>
      prev.map((p, i) =>
        i === selectedIdx
          ? {
              ...p,
              [field]: value,
            }
          : p
      )
    );
  };

  const calculateStayMinutes = (startTime: string, endTime: string) => {
    if (!startTime || !endTime) return null;

    const [startHour, startMinute] = startTime.split(":").map(Number);
    const [endHour, endMinute] = endTime.split(":").map(Number);

    const startTotalMinutes = startHour * 60 + startMinute;
    const endTotalMinutes = endHour * 60 + endMinute;

    const diff = endTotalMinutes - startTotalMinutes;

    if (diff < 0) return null;

    return diff;
  };

  const updateTimeAndStay = (
    field: "startTime" | "endTime",
    value: string
  ) => {
    setPath((prev: any[]) =>
      prev.map((p, i) => {
        if (i !== selectedIdx) return p;

        const updatePoint = {
          ...p,
          [field]: value,
        };

        const stayMinutes = calculateStayMinutes(
          updatePoint.startTime || "",
          updatePoint.endTime || ""
        );

        return {
          ...updatePoint,
          estimatedStayMinutes:
            stayMinutes !== null ? stayMinutes : updatePoint.estimatedStayMinutes,
        };
      })
    );
  };

  return (
    <div
      style={{
        width: "360px",
        background: "white",
        boxShadow: "-5px 0 20px rgba(0,0,0,0.05)",
        zIndex: 11,
        display: "flex",
        flexDirection: "column",
        height: "100%",
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "flex-end",
          padding: "12px 16px 0 16px",
        }}
      >
        <button
          onClick={onClose}
          style={{
            border: "none",
            background: "none",
            cursor: "pointer",
            display: "flex",
            alignItems: "center",
            gap: "4px",
            color: "#999",
            fontSize: "13px",
            padding: "4px 8px",
            borderRadius: "4px",
            transition: "background 0.2s",
          }}
          onMouseEnter={(e) => (e.currentTarget.style.background = "#f5f5f5")}
          onMouseLeave={(e) => (e.currentTarget.style.background = "none")}
        >
          <span style={{ fontSize: "18px", lineHeight: "1" }}>×</span>
          <span>닫기</span>
        </button>
      </div>

      <div
        style={{
          padding: "12px 24px 24px 24px",
          borderBottom: "1px solid #f0f0f0",
        }}
      >
        {point.photos && point.photos.length > 0 ? (
          <div
            style={{
              width: "100%",
              height: "180px",
              marginBottom: "15px",
              borderRadius: "12px",
              overflow: "hidden",
            }}
          >
            <img
              src={point.photos[0]}
              alt="place"
              style={{ width: "100%", height: "100%", objectFit: "cover" }}
            />
          </div>
        ) : (
          <div
            style={{
              width: "100%",
              height: "120px",
              backgroundColor: "#f5f5f5",
              borderRadius: "12px",
              marginBottom: "15px",
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              color: "#ccc",
              fontSize: "12px",
            }}
          >
            이미지가 없습니다
          </div>
        )}

        <h3 style={{ margin: "0 0 8px 0", fontSize: "20px" }}>{point.name}</h3>
        <p style={{ fontSize: "13px", color: "#666", marginBottom: "15px" }}>
          {point.address}
        </p>

        <button
          onClick={() =>
            window.open(
              `https://map.naver.com/v5/search/${encodeURIComponent(point.name)}`,
              "_blank"
            )
          }
          style={{
            width: "100%",
            padding: "10px",
            borderRadius: "8px",
            background: "#03C75A",
            color: "white",
            border: "none",
            fontSize: "13px",
            fontWeight: "bold",
            cursor: "pointer",
          }}
        >
          N 네이버 지도로 보기
        </button>
      </div>

      <div style={{ flex: 1, overflowY: "auto", padding: "20px" }}>
        <div style={{ marginBottom: "18px" }}>
          <label
            style={{
              fontSize: "12px",
              fontWeight: "bold",
              color: "#666",
              display: "block",
              marginBottom: "6px",
            }}
          >
            일정 제목
          </label>
          <input
            type="text"
            value={point.customTitle || ""}
            onChange={(e) => updateSelectedPoint("customTitle", e.target.value)}
            placeholder="예: 점심 식사, 카페 방문"
            readOnly={isReadOnly}  // ✅
            style={{
              width: "100%", padding: "10px 12px", border: "1px solid #ddd",
              borderRadius: "8px", fontSize: "14px", boxSizing: "border-box",
              background: isReadOnly ? "#f5f5f5" : "white",  // ✅ 시각적 표시
              cursor: isReadOnly ? "not-allowed" : "text",
            }}
          />
        </div>

        <div style={{ marginBottom: "18px" }}>
          <label
            style={{
              fontSize: "12px",
              fontWeight: "bold",
              color: "#666",
              display: "block",
              marginBottom: "6px",
            }}
          >
            일차
          </label>
          <input
            type="number"
            min={1}
            value={point.dayNumber === 0 ? "" : (point.dayNumber ?? "")}
            onChange={(e) => {
              const raw = e.target.value;
              if (raw === "") {
                updateSelectedPoint("dayNumber", raw === "" ? 0 : Number(raw)); // 또는 0, 빈 상태 표현
              } else {
                updateSelectedPoint("dayNumber", Number(raw));
              }
            }}
            readOnly={isReadOnly}
            style={{
              width: "100%",
              padding: "10px 12px",
              border: "1px solid #ddd",
              borderRadius: "8px",
              fontSize: "14px",
              boxSizing: "border-box",
              background: isReadOnly ? "#f5f5f5" : "white",
              cursor: isReadOnly ? "not-allowed" : "text",
            }}
          />
        </div>      
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr",
            gap: "12px",
            marginBottom: "18px",
          }}
        >
          <div>
            <label
              style={{
                fontSize: "12px",
                fontWeight: "bold",
                color: "#666",
                display: "block",
                marginBottom: "6px",
              }}
            >
              시작 시간
            </label>
            <input
              type="time"
              value={point.startTime || ""}
              onChange={(e) => updateTimeAndStay("startTime", e.target.value)}
              style={{
                width: "100%",
                padding: "10px 12px",
                border: "1px solid #ddd",
                borderRadius: "8px",
                fontSize: "14px",
                boxSizing: "border-box",background: isReadOnly ? "#f5f5f5" : "white", cursor: isReadOnly ? "not-allowed" : "text" 
              }}
            />
          </div>

          <div>
            <label
              style={{
                fontSize: "12px",
                fontWeight: "bold",
                color: "#666",
                display: "block",
                marginBottom: "6px",
              }}
            >
              종료 시간
            </label>
            <input
              type="time"
              value={point.endTime || ""}
              onChange={(e) => updateTimeAndStay("endTime", e.target.value)}
              style={{
                width: "100%",
                padding: "10px 12px",
                border: "1px solid #ddd",
                borderRadius: "8px",
                fontSize: "14px",
                boxSizing: "border-box", background: isReadOnly ? "#f5f5f5" : "white", cursor: isReadOnly ? "not-allowed" : "text"
              }}
            />
          </div>
        </div>

        <div style={{ marginBottom: "18px" }}>
          <label
            style={{
              fontSize: "12px",
              fontWeight: "bold",
              color: "#666",
              display: "block",
              marginBottom: "6px",
            }}
          >
            예상 체류 시간(분)
          </label>
          <input
            type="number"
            min={0}
            step={10}
            value={point.estimatedStayMinutes ?? 60}
            onChange={(e) =>
              updateSelectedPoint(
                "estimatedStayMinutes",
                Number(e.target.value)
              )
            }
            style={{
              width: "100%",
              padding: "10px 12px",
              border: "1px solid #ddd",
              borderRadius: "8px",
              fontSize: "14px",
              boxSizing: "border-box", background: isReadOnly ? "#f5f5f5" : "white", cursor: isReadOnly ? "not-allowed" : "text"
            }}
          />
        </div>

        <label
          style={{
            fontSize: "12px",
            fontWeight: "bold",
            color: "#666",
            display: "block",
            marginBottom: "6px",
          }}
        >
          상세 메모
        </label>

        <LocalMemoEditor
          initialMemo={point.memo || ""}
          onSave={(newMemo) => {
            if (isReadOnly) return;  // ✅
            setPath((prev: any[]) =>
              prev.map((p, i) => i === selectedIdx ? { ...p, memo: newMemo } : p)
            );
          }}
          isReadOnly={isReadOnly}  // ✅ LocalMemoEditor도 prop 받아야 함
        />
      </div>
    </div>
  );
}