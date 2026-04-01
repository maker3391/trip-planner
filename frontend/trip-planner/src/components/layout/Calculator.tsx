// Calculator.tsx
import React, { useState, useEffect } from 'react';
import './Calculator.css';
import { CalculatorService, ExpenseItem } from './calculator';

const Calculator: React.FC = () => {
  const [isCalcOpen, setIsCalcOpen] = useState(false);
  const [cart, setCart] = useState<ExpenseItem[]>([]);
  const [pendingItem, setPendingItem] = useState<ExpenseItem | null>(null);

  useEffect(() => {
    const handleOpen = () => setIsCalcOpen(true);
    const handlePrompt = (e: Event) => {
      const customEvent = e as CustomEvent;
      // 고유 ID를 부여하여 대기 상태로 저장
      setPendingItem({ ...customEvent.detail, id: Date.now().toString() });
    };
    const handleUpdate = (e: Event) => {
      const customEvent = e as CustomEvent;
      setCart(customEvent.detail);
    };

    window.addEventListener('OPEN_CALCULATOR', handleOpen);
    window.addEventListener('PROMPT_EXPENSE', handlePrompt);
    window.addEventListener('CART_UPDATED', handleUpdate);

    return () => {
      window.removeEventListener('OPEN_CALCULATOR', handleOpen);
      window.removeEventListener('PROMPT_EXPENSE', handlePrompt);
      window.removeEventListener('CART_UPDATED', handleUpdate);
    };
  }, []);

  const handleConfirmAdd = () => {
    if (pendingItem) {
      CalculatorService.addItem(pendingItem);
      setPendingItem(null);
      setIsCalcOpen(true); // 추가 후 장바구니를 자동으로 보여줌
    }
  };

  const handleCancelAdd = () => {
    setPendingItem(null);
  };

  return (
    <>
      {/* 1. 장소 소비 확인 팝업 (조건 5) */}
      {pendingItem && (
        <div className="calc-overlay">
          <div className="calc-confirm-popup">
            <h3>일정 추가 확인</h3>
            <p><strong>{pendingItem.name}</strong>({pendingItem.category})에서 일정을 진행하시겠습니까?</p>
            <p>예상 비용: {pendingItem.price.toLocaleString()}원</p>
            <div className="calc-buttons">
              <button className="calc-btn-confirm" onClick={handleConfirmAdd}>예 (장바구니 추가)</button>
              <button className="calc-btn-cancel" onClick={handleCancelAdd}>아니오</button>
            </div>
          </div>
        </div>
      )}

      {/* 2. 장바구니 / 계산기 팝업 (조건 1, 7) */}
      {isCalcOpen && (
        <div className="calc-popup-container">
          <div className="calc-header">
            <h3>장바구니</h3>
            <button className="calc-close-btn" onClick={() => setIsCalcOpen(false)}>✕</button>
          </div>
          
          <div className="calc-body">
            {cart.length === 0 ? (
              <p className="calc-empty">추가된 일정이 없습니다.</p>
            ) : (
              <ul className="calc-list">
                {cart.map((item) => (
                  <li key={item.id} className="calc-list-item">
                    <div>
                      <span className="calc-item-cat">[{item.category}]</span>
                      <span className="calc-item-name">{item.name}</span>
                    </div>
                    <span className="calc-item-price">{item.price.toLocaleString()}원</span>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="calc-footer">
            <div className="calc-total">
              <span>총 합계:</span>
              <strong>{CalculatorService.getTotal().toLocaleString()}원</strong>
            </div>
            <button className="calc-btn-reset" onClick={CalculatorService.clearItems}>초기화</button>
          </div>
        </div>
      )}
    </>
  );
};

export default Calculator;