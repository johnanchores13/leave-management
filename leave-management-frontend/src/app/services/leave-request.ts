import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Richiesta } from '../models/richiesta.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LeaveRequestService {
  private baseUrl = `${environment.apiUrl}/requests`;

  constructor(private http: HttpClient) { }

  getRichieste(): Observable<Richiesta[]> {
    return this.http.get<Richiesta[]>(`${this.baseUrl}/employee`);
  }

  inviaRichiesta(richiesta: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/submit`, richiesta);
  }

  approva(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/approve`, {});
  }

  rifiuta(id: number, motivo: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/reject?reason=${encodeURIComponent(motivo)}`, {});
  }

  getSaldo(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/balance`);
  }

  annullaRichiesta(requestId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${requestId}/cancel`, {}, { responseType: 'text' });
  }

  getRichiesteByManager(): Observable<Richiesta[]> {
    return this.http.get<Richiesta[]>(`${this.baseUrl}/manager/requests`);
  }

  segnaComeLetta(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/read`, {});
  }

  simulaMese(): Observable<string> {
    return this.http.post(`${this.baseUrl}/manager/simulate-month`, {}, { responseType: 'text' });
  }

  cambiaPassword(oldPassword: string, newPassword: string): Observable<string> {
    return this.http.put(`${this.baseUrl}/change-password`, { oldPassword, newPassword }, { responseType: 'text' });
  }

  segnaComeLettaManager(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/read-manager`, {});
  }

  getSaldoDipendentePerManager(employeeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/manager/employees/${employeeId}/balance`);
  }

  getProfilo(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/me`);
  }

}
