import React, { useState, useEffect } from "react";
import { useTripStore } from "../store/useTripStore";
import "./Calculator.css";

const Calculator: React.FC<{ readOnly?: boolean }> = ({ readOnly = false }) => {
  const [isCalcOpen, setIsCalcOpen] = useState(false);

  const {
    budget,
    setBudget,
    expenses,
    setExpenses,
    addExpense,
    updateExpense,
    deleteExpense,
    addSubExpense,
    updateSubExpense,
  } = useTripStore();

  // 1. 계산기 닫기 함수 (이벤트 연동 추가)
  const handleClose = () => {
    setIsCalcOpen(false);
    // 외부 ActionButtons 등에 닫혔음을 알림
    window.dispatchEvent(new Event("CLOSE_CALCULATOR"));
  };

  useEffect(() => {
    const handleOpen = () => setIsCalcOpen(true);
    const handleCloseEvent = () => setIsCalcOpen(false); // 외부 호출용

    const handleLoadData = (e: Event) => {
      const customEvent = e as CustomEvent;
      const loadedExpenses = (customEvent.detail?.expenses || []).map(
        (item: any, index: number) => ({
          // 고유 ID 생성 로직 개선 (중복 방지)
          id: item.id ?? -(Date.now() + index + Math.random()),
          amount: Number(item.amount) || 0,
          category: item.category || "ETC",
          description: item.description || "",
          subExpenses: (item.subExpenses || []).map((sub: any, subIndex: number) => ({
            id: sub.id ?? -(Date.now() + index * 1000 + subIndex + Math.random()),
            amount: Number(sub.amount) || 0,
            category: sub.category || "ETC",
            description: sub.description || "",
          })),
        })
      );

      setExpenses(loadedExpenses);
      setBudget(Number(customEvent.detail?.budget) || 0);
    };

    window.addEventListener("OPEN_CALCULATOR", handleOpen);
    window.addEventListener("CLOSE_CALCULATOR", handleCloseEvent);
    window.addEventListener("LOAD_CALCULATOR_DATA", handleLoadData);

    return () => {
      window.removeEventListener("OPEN_CALCULATOR", handleOpen);
      window.removeEventListener("CLOSE_CALCULATOR", handleCloseEvent);
      window.removeEventListener("LOAD_CALCULATOR_DATA", handleLoadData);
    };
  }, [setBudget, setExpenses]);

  // 데이터 변경 시 외부 시스템과 동기화
  useEffect(() => {
    window.dispatchEvent(
      new CustomEvent("SYNC_CALCULATOR", {
        detail: { expenses, budget },
      })
    );
  }, [expenses, budget]);

  // 상위/하위 입력값 처리 핸들러
  const handleInputChange = (
    id: number,
    field: "description" | "amount",
    value: string,
    parentId?: number
  ) => {
    const val =
      field === "amount"
        ? parseInt(value.replace(/[^0-9]/g, ""), 10) || 0
        : value;

    if (parentId !== undefined) {
      updateSubExpense(parentId, id, field, val as any);
    } else {
      updateExpense(id, field, val);
    }
  };

  const totalActualAmount = expenses.reduce(
    (sum, item) => sum + (item.amount || 0),
    0
  );

  return (
    <>
      {isCalcOpen && (
        <div className="calc-popup-container">
          <div className="calc-header">
            <h3>예산 계산기</h3>
            {/* handleClose로 교체 */}
            <button className="calc-close-btn" onClick={handleClose}>
              ✕
            </button>
          </div>

          <div className="calc-body">
            {expenses.length === 0 ? (
              <p className="calc-empty">등록된 지출 내역이 없습니다.</p>
            ) : (
              <ul className="calc-list">
                {expenses.map((item) => (
                  <li key={item.id} className="calc-group-container">
                    <div className="calc-item main-row">
                      {!readOnly && (
                        <button 
                          className="sub-add-btn" 
                          onClick={() => addSubExpense(item.id)}
                          title="하위 항목 추가"
                        >
                          +
                        </button>
                      )}
                      <input
                        className="input-name main-category-input"
                        type="text"
                        value={item.description || ""}
                        placeholder="카테고리(식비, 교통 등)"
                        onChange={(e) => handleInputChange(item.id, "description", e.target.value)}
                        readOnly={readOnly}  // ✅
                      />
                      <div className="main-amount-display">
                        {/* 상위 금액은 하위 금액의 합계이므로 읽기 전용으로 두는 것이 일반적입니다 */}
                        <span className="amount-val">{item.amount.toLocaleString()}</span>
                        <span className="unit">원</span>
                      </div>
                      <button
                        className="item-delete-btn"
                        onClick={() => deleteExpense(item.id)}
                      >
                        ✕
                      </button>
                    </div>

                    {item.subExpenses && item.subExpenses.length > 0 && (
                      <ul className="sub-calc-list">
                        {item.subExpenses.map((sub) => (
                          <li key={sub.id} className="calc-item sub-row">
                            <span className="indent-icon">└</span>
                            <input
                              className="input-name sub-input"
                              type="text"
                              value={sub.description || ""}
                              placeholder="상세 내역 입력"
                              onChange={(e) => handleInputChange(sub.id, "description", e.target.value, item.id)}
                              readOnly={readOnly}  // ✅
                            />
                            <input
                              className="input-price sub-price"
                              type="text"
                              value={sub.amount === 0 ? "" : sub.amount}
                              placeholder="0"
                              onChange={(e) => handleInputChange(sub.id, "amount", e.target.value, item.id)}
                              readOnly={readOnly}  // ✅
                            />
                            <span className="unit">원</span>
                            {!readOnly && (
                              <button className="item-delete-btn" onClick={() => deleteExpense(item.id)}>
                                ✕
                              </button>
                            )}
                          </li>
                        ))}
                      </ul>
                    )}
                  </li>
                ))}
              </ul>
            )}
            {!readOnly && (
              <button className="addButton" onClick={addExpense}>
                + 카테고리 추가
              </button>
            )}
          </div>

          <div className="calc-footer">
            <div className="calc-budget-row">
              <span>총 예산 :</span>
              <div className="budget-input-wrapper">
                <input
                  type="number"
                  value={budget || ""}
                  onChange={(e) => setBudget(Number(e.target.value))}
                  placeholder="0"
                  readOnly={readOnly}  // ✅
                />
                <span className="currency-label">원</span>
              </div>
            </div>
            <div className="calc-balance-row">
              <span className="balance-label">남은 잔액 :</span>
              <span
                className={`balance-amount ${
                  budget - totalActualAmount < 0 ? "negative" : ""
                }`}
              >
                {(budget - totalActualAmount).toLocaleString()}원
              </span>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Calculator;