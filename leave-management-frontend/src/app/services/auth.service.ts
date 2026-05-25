import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { environment } from '../../environments/environment';

interface JwtPayload {
  sub: string;
  employeeId: number;
  role: string;
  exp: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = `${environment.apiUrl}/auth`;
  private TOKEN_KEY = 'auth_token';

  constructor(private http: HttpClient) { }

  login(email: string, password: string): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/login`,
      { email, password },
      { responseType: 'text' }
    ).pipe(
      tap(token => localStorage.setItem(this.TOKEN_KEY, token))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = jwtDecode<JwtPayload>(token);
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  getEmployeeId(): number | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      return jwtDecode<JwtPayload>(token).employeeId;
    } catch {
      return null;
    }
  }

  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      return jwtDecode<JwtPayload>(token).role;
    } catch {
      return null;
    }
  }
}
