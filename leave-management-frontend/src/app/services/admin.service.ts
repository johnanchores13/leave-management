import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
    private baseUrl = `${environment.apiUrl}/admin`;

    constructor(private http: HttpClient) { }

    getDipendenti(page: number, size: number): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}/employees?page=${page}&size=${size}`);
    }

    getResponsabili(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/managers`);
    }


    creaDipendente(data: any): Observable<string> {
        return this.http.post(`${this.baseUrl}/employees`, data, { responseType: 'text' });
    }

    aggiornaDipendente(employeeId: number, data: any): Observable<string> {
        return this.http.put(`${this.baseUrl}/employees/${employeeId}`, data, { responseType: 'text' });
    }

    getReparti(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/departments`);
    }

    creaReparto(name: string): Observable<string> {
        return this.http.post(`${this.baseUrl}/departments`, { name }, { responseType: 'text' });
    }

    eliminaDipendente(employeeId: number): Observable<string> {
        return this.http.delete(`${this.baseUrl}/employees/${employeeId}`, { responseType: 'text' });
    }

    getSaldoDipendente(employeeId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/employees/${employeeId}/balance`);
    }

    getFestivita(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/holidays`);
    }

    aggiungiFestivita(data: string, description: string): Observable<any> {
        return this.http.post<any>(`${this.baseUrl}/holidays`, { date: data, description: description });
    }

    eliminaFestivita(id: number): Observable<any> {
        return this.http.delete(`${this.baseUrl}/holidays/${id}`, { responseType: 'text' });
    }

    aggiornaFestivita(id: number, data: string, description: string): Observable<any> {
        return this.http.put<any>(`${this.baseUrl}/holidays/${id}`, { date: data, description: description });
    }

    aggiornaReparto(id: number, name: string): Observable<any> {
        return this.http.put<any>(`${this.baseUrl}/departments/${id}`, { name });
    }

    eliminaReparto(id: number): Observable<any> {
        return this.http.delete(`${this.baseUrl}/departments/${id}`, { responseType: 'text' });
    }

    impostaSaldo(employeeId: number, body: any): Observable<string> {
        return this.http.put(
            `${this.baseUrl}/employees/${employeeId}/balance`,
            body,
            { responseType: 'text' }
        );
    }

    getTuttiSaldi(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/balances`);
    }

    simulaMese(): Observable<string> {
        return this.http.post(`${environment.apiUrl}/requests/manager/simulate-month`, {}, { responseType: 'text' });
    }

}
