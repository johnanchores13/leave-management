import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const noAuthGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (auth.isLoggedIn()) {
        const role = auth.getRole();
        if (role === 'RESPONSABILE') {
            router.navigate(['/manager']);
        } else if (role === 'ADMIN') {
            router.navigate(['/admin']);
        } else {
            router.navigate(['/dashboard']);
        }
        return false;
    }

    return true;
};
