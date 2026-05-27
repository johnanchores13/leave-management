import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
    private baseUrl = `${environment.apiUrl}/admin`;

    constructor(private http: HttpClient) { }

    getEmployees(page: number, size: number): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}/employees?page=${page}&size=${size}`);
    }

    getManagers(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/managers`);
    }


    createEmployee(data: any): Observable<string> {
        return this.http.post(`${this.baseUrl}/employees`, data, { responseType: 'text' });
    }

    updateEmployee(employeeId: number, data: any): Observable<string> {
        return this.http.put(`${this.baseUrl}/employees/${employeeId}`, data, { responseType: 'text' });
    }

    getDepartments(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/departments`);
    }

    createDepartment(name: string): Observable<string> {
        return this.http.post(`${this.baseUrl}/departments`, { name }, { responseType: 'text' });
    }

    deleteEmployee(employeeId: number): Observable<string> {
        return this.http.delete(`${this.baseUrl}/employees/${employeeId}`, { responseType: 'text' });
    }

    getEmployeeBalance(employeeId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/employees/${employeeId}/balance`);
    }

    getHolidays(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/holidays`);
    }

    addHoliday(date: string, description: string): Observable<any> {
        return this.http.post<any>(`${this.baseUrl}/holidays`, { date: date, description: description });
    }

    deleteHoliday(id: number): Observable<any> {
        return this.http.delete(`${this.baseUrl}/holidays/${id}`, { responseType: 'text' });
    }

    updateHoliday(id: number, date: string, description: string): Observable<any> {
        return this.http.put<any>(`${this.baseUrl}/holidays/${id}`, { date: date, description: description });
    }

    updateDepartment(id: number, name: string): Observable<any> {
        return this.http.put<any>(`${this.baseUrl}/departments/${id}`, { name });
    }

    deleteDepartment(id: number): Observable<any> {
        return this.http.delete(`${this.baseUrl}/departments/${id}`, { responseType: 'text' });
    }

    setEmployeeBalance(employeeId: number, body: any): Observable<string> {
        return this.http.put(
            `${this.baseUrl}/employees/${employeeId}/balance`,
            body,
            { responseType: 'text' }
        );
    }

    getAllBalances(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/balances`);
    }

    simulateMonth(): Observable<string> {
        return this.http.post(`${environment.apiUrl}/requests/manager/simulate-month`, {}, { responseType: 'text' });
    }

}
