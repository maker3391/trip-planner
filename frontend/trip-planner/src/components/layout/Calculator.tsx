// Calculator.tsx
import React, { useState, useEffect } from 'react';
import './Calculator.css';
import { CalculatorService, ExpenseItem } from './calculator';

const Calculator: React.FC = () => {
  const [isCalcOpen, setIsCalcOpen] = useState(false);
  const [cart, setCart] = useState<ExpenseItem[]>([]);
  const [pendingItem, setPendingItem] = useState<ExpenseItem | null>(null);
  const [budget, setBudget] = useState<number>(0);

  const clearItems = () => {
    window.dispatchEvent(new CustomEvent('CART_UPDATED', { detail: [] }));
    setBudget(0);
  }

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

  // 입력값 변경 핸들러
  const handleInputChange = (id: string, field: 'name' | 'price', value: string) => {
    setCart(prevCart => 
      prevCart.map(item => {
        if (item.id === id) {
          // 가격일 경우 숫자만 추출, 이름일 경우 문자열 그대로
          const updatedValue = field === 'price' ? Number(value.replace(/[^0-9]/g, '')) || 0 : value;
          return { ...item, [field]: updatedValue };
        }
        return item;
      })
    );
  };
  
  const handleAddItem = () => {
    // 서비스 함수를 호출하여 새로운 상태를 설정
    setCart(prev => CalculatorService.addItem(prev));
  };

  return (
    <>
      {/* 장바구니 / 계산기 팝업 (조건 1, 7) */}
      {isCalcOpen && (
        <div className="calc-popup-container">
          <div className="calc-header">
            <h3>예산 계산기</h3>
            <button className="calc-close-btn" onClick={() => setIsCalcOpen(false)}>✕</button>
          </div>
          
          <div className="calc-body">
            {cart.length === 0 ? (
              <p className="calc-empty">추가된 일정이 없습니다.</p>
            ) : (
              <ul className="calc-list">
                {cart.map((item) => (
                  <li key={item.id} className="calc-item">
                    <input
                      className="input-name"
                      type="text"
                      value={item.name}
                      placeholder="일정 내용"
                      onChange={(e) => handleInputChange(item.id, 'name', e.target.value)}
                    />
                    <input
                      className="input-price"
                      type="text" // 숫자를 편하게 치도록 text로 두고 가공
                      value={item.price === 0 ? '' : item.price}
                      placeholder="금액"
                      onChange={(e) => handleInputChange(item.id, 'price', e.target.value)}
                    />
                    <span className="unit"> 원</span>
                    <button className="item-delete-btn" onClick={() => CalculatorService.removeItem(cart, item.id)}>✕</button>
                  </li>
                ))}
              </ul>
            )}
            <button className="addButton" onClick={handleAddItem}>
              + 일정 추가
            </button>
          </div>

          <div className="calc-footer">
            {/* 예산 입력 행 */}
            <div className="calc-budget-row">
              <span>예산 :</span>
              <div className="budget-input-wrapper">
                <input
                  type="number"
                  value={budget || ''}
                  onChange={(e) => setBudget(Number(e.target.value))}
                  placeholder="0"
                />
                <span className="currency-label">원</span>
              </div>
            </div>
            <div className="calc-balance-row">
              <span className="balance-label">총 잔액 :</span>
              <span className="balance-amount">
                {(budget - cart.reduce((sum, item) => sum + (item.price || 0), 0)).toLocaleString()}원
              </span>
            </div>
            <button className="calc-btn-reset" onClick={clearItems}>초기화</button>
          </div>
        </div>
      )}
    </>
  );
};

export default Calculator;