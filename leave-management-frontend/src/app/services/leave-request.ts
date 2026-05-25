import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Richiesta } from '../models/richiesta.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LeaveRequestService {
  private baseUrl = `${environment.apiUrl}/richieste`;

  constructor(private http: HttpClient) { }

  getRichieste(): Observable<Richiesta[]> {
    return this.http.get<Richiesta[]>(`${this.baseUrl}/dipendente`);
  }

  inviaRichiesta(richiesta: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/invia`, richiesta);
  }

  approva(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/approva`, {});
  }

  rifiuta(id: number, motivo: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/rifiuta?reason=${encodeURIComponent(motivo)}`, {});
  }

  getSaldo(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/saldo`);
  }

  annullaRichiesta(requestId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${requestId}/annulla`, {}, { responseType: 'text' });
  }

  getRichiesteByManager(): Observable<Richiesta[]> {
    return this.http.get<Richiesta[]>(`${this.baseUrl}/manager/richieste`);
  }

  segnaComeLetta(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/leggi`, {});
  }

  simulaMese(): Observable<string> {
    return this.http.post(`${this.baseUrl}/manager/simula-mese`, {}, { responseType: 'text' });
  }

  cambiaPassword(oldPassword: string, newPassword: string): Observable<string> {
    return this.http.put(`${this.baseUrl}/cambia-password`, { oldPassword, newPassword }, { responseType: 'text' });
  }

  segnaComeLettaManager(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/leggi-manager`, {});
  }

  getSaldoDipendentePerManager(employeeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/manager/dipendenti/${employeeId}/saldo`);
  }

  getProfilo(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/me`);
  }

}