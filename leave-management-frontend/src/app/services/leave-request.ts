import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LeaveRequest } from '../models/LeaveRequest.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LeaveRequestService {
  private baseUrl = `${environment.apiUrl}/requests`;

  constructor(private http: HttpClient) { }

  getRequests(): Observable<LeaveRequest[]> {
    return this.http.get<LeaveRequest[]>(`${this.baseUrl}/employee`);
  }

  submitRequest(richiesta: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/submit`, richiesta);
  }

  approve(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/approve`, {});
  }

  reject(id: number, motivo: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/reject?reason=${encodeURIComponent(motivo)}`, {});
  }

  getBalance(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/balance`);
  }

  cancelRequest(requestId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${requestId}/cancel`, {}, { responseType: 'text' });
  }

  getManagerRequests(): Observable<LeaveRequest[]> {
    return this.http.get<LeaveRequest[]>(`${this.baseUrl}/manager/requests`);
  }

  markAsReadEmployee(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/read`, {});
  }

  simulateMonth(): Observable<string> {
    return this.http.post(`${this.baseUrl}/manager/simulate-month`, {}, { responseType: 'text' });
  }

  changePassword(oldPassword: string, newPassword: string): Observable<string> {
    return this.http.put(`${this.baseUrl}/change-password`, { oldPassword, newPassword }, { responseType: 'text' });
  }

  markAsReadByManager(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/read-manager`, {});
  }

  getEmployeeBalanceForManager(employeeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/manager/employees/${employeeId}/balance`);
  }

  getProfile(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/me`);
  }

}
