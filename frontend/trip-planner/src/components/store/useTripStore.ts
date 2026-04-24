import { create } from "zustand";

interface SubExpense {
  id: number;
  description: string;
  amount: number;
}

interface ExpenseItem {
  id: number;
  amount: number;
  category: string;
  description: string;
  subExpenses: SubExpense[];
}

interface TripState {
  tripForm: {
    title: string;
    destination: string;
    startDate: string;
    endDate: string;
  };
  setTripForm: (form: any) => void;

  budget: number;
  setBudget: (budget: number) => void;
  expenses: ExpenseItem[];
  setExpenses: (expenses: ExpenseItem[]) => void;

  addExpense: () => void;
  updateExpense: (id: number, field: string, value: any) => void;
  deleteExpense: (id: number) => void;

  // 하위 항목 액션
  addSubExpense: (parentId: number) => void;
  updateSubExpense: (
    parentId: number,
    subId: number,
    field: "description" | "amount",
    value: string | number
  ) => void;
  deleteSubSubExpense: (parentId: number, subId: number) => void;

  loadInitialData: (form: any, budget: number, expenses: ExpenseItem[]) => void;

  clearTripData: () => void;
}

export const useTripStore = create<TripState>((set) => ({
  tripForm: { title: "", destination: "", startDate: "", endDate: "" },
  setTripForm: (form) => set({ tripForm: form }),

  budget: 0,
  setBudget: (budget) => set({ budget: Number(budget) || 0 }),

  expenses: [],
  setExpenses: (expenses) =>
    set({
      expenses: expenses.map((e) => ({
        ...e,
        subExpenses: e.subExpenses ?? [],
      })),
    }),

  addExpense: () =>
    set((state) => ({
      expenses: [
        ...state.expenses,
        {
          id: -Date.now(),
          amount: 0,
          category: "ETC",
          description: "",
          subExpenses: [],
        },
      ],
    })),

  updateExpense: (id, field, value) =>
    set((state) => ({
      expenses: state.expenses.map((item) =>
        item.id === id ? { ...item, [field]: value } : item
      ),
    })),

  deleteExpense: (id) =>
    set((state) => ({
      expenses: state.expenses.filter((item) => item.id !== id),
    })),

  // 하위 항목 추가
  addSubExpense: (parentId) =>
    set((state) => ({
      expenses: state.expenses.map((item) =>
        item.id === parentId
          ? {
              ...item,
              subExpenses: [
                ...(item.subExpenses ?? []),
                { id: -Date.now(), description: "", amount: 0 },
              ],
            }
          : item
      ),
    })),

  // 하위 항목 수정 → 상위 amount 자동 합산
  updateSubExpense: (parentId, subId, field, value) =>
    set((state) => ({
      expenses: state.expenses.map((item) => {
        if (item.id !== parentId) return item;

        const updatedSubs = (item.subExpenses ?? []).map((sub) =>
          sub.id === subId ? { ...sub, [field]: value } : sub
        );

        const totalAmount = updatedSubs.reduce(
          (sum, sub) => sum + (sub.amount || 0),
          0
        );

        return {
          ...item,
          subExpenses: updatedSubs,
          amount: totalAmount,
        };
      }),
    })),

  // 하위 항목 삭제 → 상위 amount 재계산
  deleteSubSubExpense: (parentId, subId) =>
    set((state) => ({
      expenses: state.expenses.map((item) => {
        if (item.id !== parentId) return item;

        const filteredSubs = (item.subExpenses ?? []).filter(
          (sub) => sub.id !== subId
        );

        const totalAmount = filteredSubs.reduce(
          (sum, sub) => sum + (sub.amount || 0),
          0
        );

        return {
          ...item,
          subExpenses: filteredSubs,
          amount: totalAmount,
        };
      }),
    })),

  loadInitialData: (form, budget, expenses) =>
    set({
      tripForm: form,
      budget: Number(budget) || 0,
      expenses: expenses.map((e) => ({
        ...e,
        subExpenses: (e as any).subExpenses ?? [],
      })),
    }),

  clearTripData: () =>
    set({
      tripForm: { title: "", destination: "", startDate: "", endDate: "" },
      budget: 0,
      expenses: [],
    }),
}));