export interface Pagination<T> {
  currentPage: number;
  perPage: number;
  totalItems: number;
  items: T[];
}

export interface SessionResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  refreshToken: string;
}

export interface Address {
  street: string;
  number: string;
  complement?: string | null;
  neighborhood: string;
  city: string;
  state: string;
  country: string;
  zipCode: string;
}

export interface ShowSummary {
  id: string;
  name: string;
  description: string;
  date: string;
  address: Address;
  published: boolean;
  totalSpots: number;
  totalSpotsSold: number;
  partnerId: string;
}

export type ShowDetail = ShowSummary;

export interface SpotItem {
  id: string;
  location: string;
  available: boolean;
  published: boolean;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

export interface OrderResponse {
  orderId: string;
  customerId: string;
  status: 'PENDING' | 'PAID' | 'EXPIRED' | 'CANCELLED' | 'REFUNDED';
  totalValue: number;
  currency: string;
  expiresAt: string;
  chargeId: string | null;
  spotIds: string[];
}

export interface PayOrderResponse {
  orderId: string;
  chargeId: string;
  paymentCode: string;
  chargeStatus: string;
}

export interface ApiError {
  errors?: { message: string }[];
  message?: string;
}
