import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    const expectedRole = route.data['role'] as string;
    const userRole = auth.getRole();

    if (userRole === expectedRole) {
        return true;
    }

    if (userRole === 'ADMIN') {
        router.navigate(['/admin']);
    } else if (userRole === 'RESPONSABILE') {
        router.navigate(['/manager']);
    } else {
        router.navigate(['/dashboard']);
    }
    return false;
};