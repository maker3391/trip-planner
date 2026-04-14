import React, { useState, useEffect } from "react";
import { useTripStore } from "../store/useTripStore";
import "./Calculator.css";

const Calculator: React.FC = () => {
  const [isCalcOpen, setIsCalcOpen] = useState(false);

  const {
    budget,
    setBudget,
    expenses,
    setExpenses,
    addExpense,
    updateExpense,
    deleteExpense,
  } = useTripStore();

  useEffect(() => {
    const handleOpen = () => setIsCalcOpen(true);

    const handleLoadData = (e: Event) => {
      const customEvent = e as CustomEvent;
      const loadedExpenses = (customEvent.detail?.expenses || []).map(
        (item: any, index: number) => ({
          id: item.id ?? -(Date.now() + index),
          amount: Number(item.amount) || 0,
          category: item.category || "ETC",
          description: item.description || "",
        })
      );

      setExpenses(loadedExpenses);
      setBudget(Number(customEvent.detail?.budget) || 0);
    };

    window.addEventListener("OPEN_CALCULATOR", handleOpen);
    window.addEventListener("LOAD_CALCULATOR_DATA", handleLoadData);

    return () => {
      window.removeEventListener("OPEN_CALCULATOR", handleOpen);
      window.removeEventListener("LOAD_CALCULATOR_DATA", handleLoadData);
    };
  }, [setBudget, setExpenses]);

  useEffect(() => {
    window.dispatchEvent(
      new CustomEvent("SYNC_CALCULATOR", {
        detail: {
          expenses,
          budget,
        },
      })
    );
  }, [expenses, budget]);

  const handleInputChange = (
    id: number,
    field: "description" | "amount",
    value: string
  ) => {
    const val =
      field === "amount"
        ? parseInt(value.replace(/[^0-9]/g, ""), 10) || 0
        : value;

    updateExpense(id, field, val);
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
            <button className="calc-close-btn" onClick={() => setIsCalcOpen(false)}>
              ✕
            </button>
          </div>

          <div className="calc-body">
            {expenses.length === 0 ? (
              <p className="calc-empty">등록된 지출 내역이 없습니다.</p>
            ) : (
              <ul className="calc-list">
                {expenses.map((item) => (
                  <li key={item.id} className="calc-item">
                    <input
                      className="input-name"
                      type="text"
                      value={item.description || ""}
                      placeholder="내용 입력"
                      onChange={(e) =>
                        handleInputChange(item.id, "description", e.target.value)
                      }
                    />
                    <input
                      className="input-price"
                      type="text"
                      value={item.amount === 0 ? "" : item.amount}
                      placeholder="0"
                      onChange={(e) =>
                        handleInputChange(item.id, "amount", e.target.value)
                      }
                    />
                    <span className="unit">원</span>
                    <button
                      className="item-delete-btn"
                      onClick={() => deleteExpense(item.id)}
                    >
                      ✕
                    </button>
                  </li>
                ))}
              </ul>
            )}
            <button className="addButton" onClick={addExpense}>
              + 항목 추가
            </button>
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