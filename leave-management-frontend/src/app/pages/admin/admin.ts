import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import Swal from 'sweetalert2';


@Component({
    selector: 'app-admin',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './admin.html',
    styleUrl: './admin.css'
})
export class AdminComponent implements OnInit {

    activeSection = 'dipendenti';
    mostraModalCreaDipendente = false;
    mostraModalCreaReparto = false;
    listaDipendenti: any[] = [];
    listaReparti: any[] = [];
    dipendentiResponsabili: any[] = [];
    currentPage = 0;
    pageSize = 10;
    totalPages = 0;
    listaFestivita: any[] = [];
    festivitaForm = new FormGroup({
        date: new FormControl('', Validators.required),
        description: new FormControl('', Validators.required)
    });
    mostraModalCreaFestivita = false;
    mostraModalModificaFestivita = false;
    festivitaInModifica: any = null;
    mostraModalModificaReparto = false;
    repartoInModifica: any = null;
    listaTuttiDipendenti: any[] = [];
    annoCorrente = new Date().getFullYear();
    mostraModalModificaDipendente = false;
    dipendenteDaModificare: number | null = null;
    modificaForm = new FormGroup({
        departmentId: new FormControl(''),
        managerId: new FormControl('')
    });
    listaSaldi: any[] = [];
    mostraModalSaldo = false;
    saldoInModifica: any = null;

    constructor(
        private adminService: AdminService,
        public authService: AuthService,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.caricaDipendenti();
        this.caricaResponsabili();
        this.caricaReparti();
        this.caricaFestivita();
        this.caricaTuttiDipendenti();
        this.caricaSaldi();
    }


    cambiaSezione(sezione: string) {
        this.activeSection = sezione;
        this.closeMenu();
    }

    isMenuOpen = false;

    toggleMenu() {
        this.isMenuOpen = !this.isMenuOpen;
    }

    closeMenu() {
        this.isMenuOpen = false;
    }

    caricaDipendenti() {
        this.adminService.getDipendenti(this.currentPage, this.pageSize).subscribe({
            next: (data: any) => {
                this.listaDipendenti = data.content;
                this.totalPages = data.totalPages;
                this.cdr.detectChanges();
            },
            error: err => console.error('Errore caricamento dipendenti:', err)
        });
    }

    caricaResponsabili() {
        this.adminService.getResponsabili().subscribe({
            next: data => {
                this.dipendentiResponsabili = data;
                this.cdr.detectChanges();
            },
            error: err => console.error('Errore caricamento responsabili:', err)
        });
    }

    cambiaPagina(nuovaPagina: number) {
        if (nuovaPagina >= 0 && nuovaPagina < this.totalPages) {
            this.currentPage = nuovaPagina;
            this.caricaDipendenti();
        }
    }



    employeeForm = new FormGroup({
        firstName: new FormControl('', Validators.required),
        lastName: new FormControl('', Validators.required),
        email: new FormControl('', [Validators.required, Validators.email]),
        serialNumber: new FormControl('', Validators.required),
        password: new FormControl('', Validators.required),
        role: new FormControl('DIPENDENTE', Validators.required),
        departmentId: new FormControl(''),
        managerId: new FormControl(''),
        hiringDate: new FormControl('')
    });

    creaDipendente() {
        if (this.employeeForm.invalid) {
            Swal.fire('Attenzione', 'Compila tutti i campi obbligatori.', 'warning');
            return;
        }
        const raw = this.employeeForm.value;
        const payload = {
            ...raw,
            departmentId: raw.departmentId ? Number(raw.departmentId) : null,
            managerId: raw.managerId ? Number(raw.managerId) : null,
        };
        this.adminService.creaDipendente(payload).subscribe({
            next: () => {
                Swal.fire('Fatto!', 'Dipendente creato con successo.', 'success');
                this.employeeForm.reset({ role: 'DIPENDENTE', hiringDate: '' });
                this.mostraModalCreaDipendente = false;
                this.caricaDipendenti();
            },
            error: err => {
                const msg = err.error?.message || err.error || 'Errore durante la creazione.';
                Swal.fire('Errore', msg, 'error');
            }
        });
    }

    caricaReparti() {
        this.adminService.getReparti().subscribe({
            next: data => this.listaReparti = data,
            error: err => console.error('Errore caricamento reparti:', err)
        });
    }

    repartoForm = new FormGroup({
        name: new FormControl('', Validators.required)
    });

    salvaReparto() {
        if (this.repartoForm.invalid) return;

        if (this.repartoInModifica) {
            this.adminService.aggiornaReparto(this.repartoInModifica.departmentId, this.repartoForm.value.name!).subscribe({
                next: () => {
                    this.caricaReparti();
                    this.chiudiModalReparto();
                    Swal.fire('Successo', 'Reparto modificato!', 'success');
                },
                error: err => Swal.fire('Errore', 'Errore durante la modifica', 'error')
            });
        } else {
            this.adminService.creaReparto(this.repartoForm.value.name!).subscribe({
                next: () => {
                    this.caricaReparti();
                    this.chiudiModalReparto();
                    Swal.fire('Successo', 'Reparto creato!', 'success');
                },
                error: err => Swal.fire('Errore', 'Errore durante la creazione.', 'error')
            });
        }
    }

    eliminaReparto(id: number) {
        this.adminService.eliminaReparto(id).subscribe({
            next: () => {
                this.caricaReparti();
                Swal.fire('Successo', 'Reparto eliminato!', 'success');
            },
            error: err => Swal.fire('Errore', 'Impossibile eliminare (forse ci sono dipendenti assegnati a questo reparto?)', 'error')
        });
    }



    logout() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    eliminaDipendente(employeeId: number, nome: string) {
        Swal.fire({
            title: `Eliminare ${nome}?`,
            text: 'Questa azione è irreversibile.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#e74c3c',
            confirmButtonText: 'Sì, elimina',
            cancelButtonText: 'Annulla'
        }).then(result => {
            if (result.isConfirmed) {
                this.adminService.eliminaDipendente(employeeId).subscribe({
                    next: () => {
                        Swal.fire('Eliminato!', 'Il dipendente è stato rimosso.', 'success');
                        this.caricaDipendenti();
                    },
                    error: err => {
                        const msg = err.error?.message || err.error || 'Errore durante l\'eliminazione.';
                        Swal.fire('Errore', msg, 'error');
                    }
                });
            }
        });
    }

    vedisaldo(employeeId: number, nome: string) {
        this.adminService.getSaldoDipendente(employeeId).subscribe({
            next: (saldi) => {
                const annoCorrente = new Date().getFullYear();
                const saldoAnno = saldi.filter(s => s.referenceYear === annoCorrente);

                if (saldoAnno.length === 0) {
                    Swal.fire('Saldo', `${nome} non ha saldi per l'anno corrente.`, 'info');
                    return;
                }

                const righe = saldoAnno.map(s => `
                <tr>
                    <td style="padding: 8px 12px">${s.leaveType === 'VACATION' ? 'Ferie' : 'Permessi'}</td>
                    <td style="padding: 8px 12px">${Math.floor(s.totalQuantity)}</td>
                    <td style="padding: 8px 12px">${Math.floor(s.usedQuantity)}</td>
                    <td style="padding: 8px 12px"><strong>${Math.floor(s.remainingBalance)}</strong></td>
                </tr>
            `).join('');

                Swal.fire({
                    title: `Saldo di ${nome}`,
                    html: `
                    <table style="width:100%;border-collapse:collapse;text-align:left">
                        <thead>
                            <tr style="background:#f1f5f9;color:#64748b">
                                <th style="padding: 8px 12px">Tipo</th>
                                <th style="padding: 8px 12px">Totale</th>
                                <th style="padding: 8px 12px">Utilizzati</th>
                                <th style="padding: 8px 12px">Rimanenti</th>
                            </tr>
                        </thead>
                        <tbody>${righe}</tbody>
                    </table>
                `,
                    confirmButtonText: 'Chiudi'
                });
            },
            error: err => {
                const msg = err.error?.message || err.error || 'Errore nel caricamento del saldo.';
                Swal.fire('Errore', msg, 'error');
            }
        });
    }

    caricaFestivita() {
        this.adminService.getFestivita().subscribe({
            next: data => {
                this.listaFestivita = data;
                this.cdr.detectChanges();
            }
        });
    }

    apriModificaFestivita(festivita: any) {
        this.festivitaInModifica = festivita;
        this.festivitaForm.patchValue({
            date: festivita.date,
            description: festivita.description
        });
        this.mostraModalModificaFestivita = true;
    }

    chiudiModalFestivita() {
        this.mostraModalCreaFestivita = false;
        this.mostraModalModificaFestivita = false;
        this.festivitaInModifica = null;
        this.festivitaForm.reset();
    }

    salvaFestivita() {
        if (this.festivitaForm.invalid) return;
        const { date, description } = this.festivitaForm.value;

        if (this.festivitaInModifica) {
            this.adminService.aggiornaFestivita(this.festivitaInModifica.id, date!, description!).subscribe({
                next: () => {
                    this.caricaFestivita();
                    this.chiudiModalFestivita();
                    Swal.fire('Successo', 'Festività modificata!', 'success');
                }
            });
        } else {
            this.adminService.aggiungiFestivita(date!, description!).subscribe({
                next: () => {
                    this.caricaFestivita();
                    this.chiudiModalFestivita();
                    Swal.fire('Successo', 'Festività aggiunta!', 'success');
                }
            });
        }
    }

    eliminaFestivita(id: number) {
        Swal.fire({
            title: 'Eliminare questa festività?',
            text: 'Questa azione è irreversibile.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#e74c3c',
            confirmButtonText: 'Sì, elimina',
            cancelButtonText: 'Annulla'
        }).then(result => {
            if (result.isConfirmed) {
                this.adminService.eliminaFestivita(id).subscribe({
                    next: () => {
                        Swal.fire('Eliminata!', 'La festività è stata rimossa.', 'success');
                        this.caricaFestivita();
                    },
                    error: err => Swal.fire('Errore', 'Impossibile eliminare la festività.', 'error')
                });
            }
        });
    }

    apriModificaReparto(reparto: any) {
        this.repartoInModifica = reparto;
        this.repartoForm.patchValue({
            name: reparto.name
        });
        this.mostraModalModificaReparto = true;
    }

    chiudiModalReparto() {
        this.mostraModalCreaReparto = false;
        this.mostraModalModificaReparto = false;
        this.repartoInModifica = null;
        this.repartoForm.reset();
    }

    chiudiModalModificaDipendente() {
        this.mostraModalModificaDipendente = false;
        this.dipendenteDaModificare = null;
        this.modificaForm.reset();
    }

    salvaModificaDipendente() {
        if (!this.dipendenteDaModificare) return;

        const body: any = {};
        const { departmentId, managerId } = this.modificaForm.value;

        if (departmentId) {
            body.departmentId = departmentId;
        }
        body.managerId = managerId ? managerId : null;
        if (!departmentId && managerId === undefined) {
            Swal.fire('Attenzione', 'Seleziona almeno un campo da modificare.', 'warning');
            return;
        }

        this.adminService.aggiornaDipendente(this.dipendenteDaModificare, body).subscribe({
            next: () => {
                Swal.fire('Aggiornato!', 'Dipendente aggiornato con successo.', 'success');
                this.chiudiModalModificaDipendente();
                this.caricaDipendenti();
            },
            error: err => {
                const msg = err.error?.message || err.error || 'Errore durante la modifica.';
                Swal.fire('Errore', msg, 'error');
            }
        });

    }

    apriModificaDipendente(employeeId: number, departmentId: any, managerId: any) {
        this.dipendenteDaModificare = employeeId;
        this.modificaForm.patchValue({
            departmentId: departmentId || '',
            managerId: managerId || ''
        });
        this.mostraModalModificaDipendente = true;
    }

    saldoForm = new FormGroup({
        employeeId: new FormControl<string | number>('', Validators.required),
        leaveType: new FormControl<string>('VACATION', Validators.required),
        referenceYear: new FormControl<string | number>('', Validators.required),
        totalQuantity: new FormControl<string | number>('', Validators.required)
    });

    caricaTuttiDipendenti() {
        this.adminService.getDipendenti(0, 1000).subscribe({
            next: (data: any) => {
                this.listaTuttiDipendenti = data.content;
                this.cdr.detectChanges();
            },
            error: err => console.error('Errore caricamento dipendenti:', err)
        });
    }

    caricaSaldi() {
        this.adminService.getTuttiSaldi().subscribe({
            next: data => {
                this.listaSaldi = data;
                this.cdr.detectChanges();
            },
            error: err => console.error('Errore caricamento saldi:', err)
        });
    }

    apriCreaSaldo() {
        this.saldoInModifica = null;
        this.saldoForm.reset({ leaveType: 'VACATION', referenceYear: this.annoCorrente });
        this.mostraModalSaldo = true;
    }

    apriModificaSaldo(saldo: any) {
        this.saldoInModifica = saldo;
        this.saldoForm.patchValue({
            employeeId: saldo.employeeId,
            leaveType: saldo.leaveType,
            referenceYear: saldo.referenceYear,
            totalQuantity: saldo.totalQuantity
        });
        this.mostraModalSaldo = true;
    }

    chiudiModalSaldo() {
        this.mostraModalSaldo = false;
        this.saldoInModifica = null;
        this.saldoForm.reset({ leaveType: 'VACATION' });
    }

    impostaSaldo() {
        if (this.saldoForm.invalid) {
            Swal.fire('Attenzione', 'Compila tutti i campi.', 'warning');
            return;
        }
        const { employeeId, leaveType, referenceYear, totalQuantity } = this.saldoForm.value;
        this.adminService.impostaSaldo(Number(employeeId), {
            leaveType,
            referenceYear: Number(referenceYear),
            totalQuantity: Number(totalQuantity)
        }).subscribe({
            next: () => {
                Swal.fire('Fatto!', 'Saldo aggiornato con successo.', 'success');
                this.chiudiModalSaldo();
                this.caricaSaldi();
            },
            error: err => {
                const msg = err.error?.message || err.error || 'Errore durante il salvataggio.';
                Swal.fire('Errore', msg, 'error');
            }
        });
    }

    simulaMese() {
        Swal.fire({
            title: 'Simulare il mese?',
            text: 'Verranno accreditati ferie e permessi a tutti i dipendenti.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sì, simula',
            cancelButtonText: 'Annulla'
        }).then(result => {
            if (result.isConfirmed) {
                this.adminService.simulaMese().subscribe({
                    next: () => Swal.fire('Fatto!', 'Ferie e permessi accreditati.', 'success'),
                    error: () => Swal.fire('Errore', 'Impossibile simulare il mese.', 'error')
                });
            }
        });
    }

}