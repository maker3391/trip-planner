import { create } from 'zustand';

interface ExpenseItem {
  id: number;
  amount: number;
  category: string;
  description: string;
}

interface TripState {
  tripForm: { title: string; destination: string; startDate: string; endDate: string };
  setTripForm: (form: any) => void;

  budget: number;
  setBudget: (budget: number) => void;
  expenses: ExpenseItem[];
  setExpenses: (expenses: ExpenseItem[]) => void;

  addExpense: () => void;
  updateExpense: (id: number, field: string, value: any) => void;
  deleteExpense: (id: number) => void;

  loadInitialData: (form: any, budget: number, expenses: ExpenseItem[]) => void;
  
  clearTripData: () => void;
}

export const useTripStore = create<TripState>((set) => ({
  tripForm: { title: "", destination: "", startDate: "", endDate: "" },
  setTripForm: (form) => set({ tripForm: form }),

  budget: 0,
  setBudget: (budget) => set({ budget }),

  expenses: [],
  setExpenses: (expenses) => set({ expenses }),

  addExpense: () => set((state) => ({
    expenses: [...state.expenses, { id: -Date.now(), amount: 0, category: 'ETC', description: '' }]
  })),

  updateExpense: (id, field, value) => set((state) => ({
    expenses: state.expenses.map((item) =>
      item.id === id ? { ...item, [field]: value } : item
    )
  })),

  deleteExpense: (id) => set((state) => ({
    expenses: state.expenses.filter((item) => item.id !== id)
  })),

  loadInitialData: (form, budget, expenses) => set({
    tripForm: form,
    budget: budget,
    expenses: expenses
  }),

  clearTripData: () => set({
    tripForm: { title: "", destination: "", startDate: "", endDate: "" },
    budget: 0,
    expenses: []
  })
}));