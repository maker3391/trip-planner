// calculator.ts

export interface ExpenseItem {
  id: string;
  name: string;
  category: string; // 예: '식당', '호텔', '체험'
  price: number;
}

// 장바구니 데이터 (초기화 전까지 전역으로 유지됨)
let cart: ExpenseItem[] = [];

export const CalculatorService = {
  // 1. 계산기 팝업 열기 (버튼 onClick 용)
  openCalculator: () => {
    window.dispatchEvent(new CustomEvent('OPEN_CALCULATOR'));
  },

  // 2. 지도 API에서 특정 장소 클릭 시 소비 여부 묻는 팝업 띄우기
  promptExpense: (itemInfo: Omit<ExpenseItem, 'id'>) => {
    window.dispatchEvent(new CustomEvent('PROMPT_EXPENSE', { detail: itemInfo }));
  },

  // 3. 장바구니에 추가
  addItem: (item: ExpenseItem) => {
    cart.push(item);
    window.dispatchEvent(new CustomEvent('CART_UPDATED', { detail: [...cart] }));
  },

  // 4. 장바구니 초기화
  clearItems: () => {
    cart = [];
    window.dispatchEvent(new CustomEvent('CART_UPDATED', { detail: [] }));
  },

  // 현재 총합 계산
  getTotal: () => {
    return cart.reduce((sum, item) => sum + item.price, 0);
  }
};