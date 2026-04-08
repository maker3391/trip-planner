export interface ExpenseRequest {
  amount: number;
  category: string;
  description: string;
}

export interface ExpenseResponse {
  id: number;
  amount: number;
  category: string;
  description: string;
}

export interface ExpenseSummaryResponse {
  totalBudget: number;
  totalPlannedAmount: number;
  totalActualAmount: number;
  remainingBudget: number;
  planVsActualGap: number;
  budgetUsagePercentage: number;
}