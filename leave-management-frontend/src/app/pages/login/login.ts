import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [FormsModule, CommonModule],
    templateUrl: './login.html',
    styleUrl: './login.css'
})
export class LoginComponent {
    email = '';
    password = '';
    errore = '';

    constructor(private authService: AuthService, private router: Router) { }

    login() {
        this.authService.login(this.email, this.password).subscribe({
            next: () => {
                const role = this.authService.getRole();
                if (role === 'ADMIN') {
                    this.router.navigate(['/admin']);
                } else {
                    this.router.navigate(['/dashboard']);
                }
            },
            error: () => {
                this.errore = 'Email o password errati.';
            }
        });
    }
}
