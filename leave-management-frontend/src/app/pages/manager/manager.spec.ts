import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ManagerComponent } from './manager';

describe('Manager', () => {
  let component: ManagerComponent;
  let fixture: ComponentFixture<ManagerComponent>;



  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManagerComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(ManagerComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
