import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
    private baseUrl = `${environment.apiUrl}/admin`;

    constructor(private http: HttpClient) { }

    getDipendenti(page: number, size: number): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}/dipendenti?page=${page}&size=${size}`);
    }

    getResponsabili(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/responsabili`);
    }


    creaDipendente(data: any): Observable<string> {
        return this.http.post(`${this.baseUrl}/dipendenti`, data, { responseType: 'text' });
    }

    aggiornaDipendente(employeeId: number, data: any): Observable<string> {
        return this.http.put(`${this.baseUrl}/dipendenti/${employeeId}`, data, { responseType: 'text' });
    }

    getReparti(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/reparti`);
    }

    creaReparto(name: string): Observable<string> {
        return this.http.post(`${this.baseUrl}/reparti`, { name }, { responseType: 'text' });
    }

    eliminaDipendente(employeeId: number): Observable<string> {
        return this.http.delete(`${this.baseUrl}/dipendenti/${employeeId}`, { responseType: 'text' });
    }

    getSaldoDipendente(employeeId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/dipendenti/${employeeId}/saldo`);
    }

    getFestivita(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/festivita`);
    }

    aggiungiFestivita(data: string, description: string): Observable<any> {
        return this.http.post<any>(`${this.baseUrl}/festivita`, { date: data, description: description });
    }

    eliminaFestivita(id: number): Observable<any> {
        return this.http.delete(`${this.baseUrl}/festivita/${id}`, { responseType: 'text' });
    }

    aggiornaFestivita(id: number, data: string, description: string): Observable<any> {
        return this.http.put<any>(`${this.baseUrl}/festivita/${id}`, { date: data, description: description });
    }

    aggiornaReparto(id: number, name: string): Observable<any> {
        return this.http.put<any>(`${this.baseUrl}/reparti/${id}`, { name });
    }

    eliminaReparto(id: number): Observable<any> {
        return this.http.delete(`${this.baseUrl}/reparti/${id}`, { responseType: 'text' });
    }

    impostaSaldo(employeeId: number, body: any): Observable<string> {
        return this.http.put(
            `${this.baseUrl}/dipendenti/${employeeId}/saldo`,
            body,
            { responseType: 'text' }
        );
    }

    getTuttiSaldi(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/saldi`);
    }

    simulaMese(): Observable<string> {
        return this.http.post(`${environment.apiUrl}/richieste/manager/simula-mese`, {}, { responseType: 'text' });
    }

}