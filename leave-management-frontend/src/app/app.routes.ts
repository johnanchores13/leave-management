import { Routes } from '@angular/router';
import { EmployeeComponent } from './pages/employee/employee';
import { ManagerComponent } from './pages/manager/manager';
import { LoginComponent } from './pages/login/login';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';
import { noAuthGuard } from './guards/no-auth.guard';
import { AdminComponent } from './pages/admin/admin';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [noAuthGuard] },
  {
    path: 'dashboard',
    component: EmployeeComponent,
    canActivate: [authGuard]
  },

  {
    path: 'manager',
    component: ManagerComponent,
    canActivate: [authGuard, roleGuard],
    data: { role: 'RESPONSABILE' }
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [authGuard, roleGuard],
    data: { role: 'ADMIN' }
  }

];