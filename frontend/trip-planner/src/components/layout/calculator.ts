// calculator.ts

export interface ExpenseItem {
  id: string; // 고유 ID (예: 타임스탬프 기반)
  name: string;
  price: number;
}

// 장바구니 데이터 (초기화 전까지 전역으로 유지됨)
const cart: ExpenseItem[] = [];

export const CalculatorService = {
  // 1. 계산기 팝업 열기 (버튼 onClick 용)
  openCalculator: () => {
    window.dispatchEvent(new CustomEvent('OPEN_CALCULATOR'));
  },

  addItem: (prevCart: ExpenseItem[]): ExpenseItem[] => {
    const newItem: ExpenseItem = {
      id: Date.now().toString(), // 고유 ID 생성 (타임스탬프 기반)
      name: '', // 초기값은 빈 문자열
      price: 0,
    };
    return [...prevCart, newItem];
  },

  // 특정 아이템의 내용을 수정하는 방식
  updateItem: (cart: ExpenseItem[], id: string, newName: string): ExpenseItem[] => {
    return cart.map(item => item.id === id ? { ...item, name: newName } : item);
  },

  // 특정 아이템 삭제 (현재 cart 배열과 삭제할 id를 인자로 받음)
  removeItem: (currentCart: ExpenseItem[], id: string) => {
    const updatedCart = currentCart.filter(item => item.id !== id);
    window.dispatchEvent(new CustomEvent('CART_UPDATED', { detail: updatedCart }));
  },

  getTotal: (currentCart: ExpenseItem[]) => {
    return currentCart.reduce((sum, item) => sum + (Number(item.price) || 0), 0);
  }
};