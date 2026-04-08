import client from "./client";
import { ExpenseRequest, ExpenseResponse, ExpenseSummaryResponse } from "../../types/expense";

export const getExpenseSummary = async (tripId: number): Promise<ExpenseSummaryResponse> => {
  const response = await client.get(`/trips/${tripId}/expenses`);
  return response.data;
};

export const getExpense = async (tripId: number): Promise<ExpenseResponse[]> => {
  const response = await client.get(`/trips/${tripId}/expenses`);
  return response.data;
};

export const addExpense = async (tripId: number, data: ExpenseRequest): Promise<ExpenseResponse> => {
  const response = await client.post(`/trips/${tripId}/expenses`, data);
  return response.data;
};

export const updateExpense = async (expenseId: number, data: ExpenseRequest): Promise<ExpenseResponse> => {
  const response = await client.put(`/expenses/${expenseId}`, data);
  return response.data;
};

export const deleteExpense = async (expenseId: number): Promise<void> => {
  await client.delete(`/expenses/${expenseId}`);
};