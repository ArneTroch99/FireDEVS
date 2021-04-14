package CellularAutomata.FF_Rothermel;

import java.lang.Math;

/**
 * @author Siemen & Arne
 *
 * Based on
 *        Rothermell explained: https://www.fs.fed.us/rm/pubs_series/rmrs/gtr/rmrs_gtr371.pdf
 *        Original Rothermell paper: https://www.fs.fed.us/rm/pubs_int/int_rp115.pdf?fbclid=IwAR2zoRQIDdxgSL_OxlV0Z5weuGw8dbyzw1TrQa-bXp6RJRT1NXjLtYy8zUk
 *        Fuel models: https://www.fs.fed.us/rm/pubs/rmrs_gtr153.pdf?fbclid=IwAR1KRhj51mhYMn2vL7cx-dAZwmXvyJKt1RRADv1JmxqFkJZC6xPqLCh1Auw
 */
public class ROS_Calculator {

    private double w_o;
    private double sigma;
    private double h;
    private double M_f;
    private double U;
    private double beta;
    private double M_x;

    private final double S_t = 0.0555;  // Fuel particle total mineral content: minerals(lb)/over-dry-woord(lb)
    private final double S_e = 0.010;   // Fuel particle effective mineral content: silica-free-minerals(lb)/oven-dry-wood(lb)

    public static void main(String[] args) {
        ROS_Calculator rc = new ROS_Calculator();
        System.out.println(rc.calculate_ROS(0.02, 0.4, 2054, 8000, 0.00143, 0.14,440, 0, 0.15));
    }

    /**
     * @param w_o     Overdry fuel loading: lb/ft²
     * @param delta   Fuel depth: ft
     * @param sigma   Fuel particle surface-area-to-volume ratio: 1/ft
     * @param h       Fuel particle low heat content: b.t.u./lb (BTU is energy unit)
     * @param beta    Packing ratio
     * @param m_f     Fuel particle moisture content: moisture(lb)/over-dry-wood(lb)
     * @param u       Wind velocity at mid-flame heigt ft/min
     * @param tan_phi Slope, vertical rise/horizontal rise
     * @param M_x     Moisture content of extinction
     * @return ROS Rate of spread: ft/min
     */
    public double calculate_ROS(double w_o, double delta, double sigma, double h, double beta, double m_f,
                                double u, double tan_phi, double M_x) {
        this.w_o = w_o;
        this.sigma = sigma;
        this.h = h;
        this.M_f = m_f;
        this.U = u;
        this.beta = beta;
        this.M_x = M_x;

        double rho_b = w_o / delta;                                         // Oven dry bulk density: lb/ft³
        double I_R = calculateI_R();                                        // Reaction intensity
        double xi = calculate_xi();                                         // Propagating flux ratio
        double phi_w = calculate_phi_w();                                   // Wind factor
        double phi_s = 5.275 * Math.pow(beta, -0.3) * tan_phi * tan_phi;    // Slope factor
        double epsilon = Math.exp(-138 / sigma);                            // Effective heating number
        double Q_ig = 250 + 1116 * M_f;                                     // Heat of preignition: b.t.u/lb

        return (I_R * xi * (1 + phi_w + phi_s)) / (rho_b * epsilon * Q_ig);
    }


    // Calculating the raction instensity:

    /**
     * @return The reaction intensity: b.t.u/ (ft² * min)
     */
    private double calculateI_R() {
        double gamma_prime = calculate_gamme_prime();
        double n_M = calculate_n_M();
        double w_n = w_o * (1 - S_t);
        double n_S = 0.174 * Math.pow(S_e, -0.19);

        return gamma_prime * w_n * h * n_M * n_S;
    }

    private double calculate_gamme_prime() {
        double gamma_prime_max = Math.pow(sigma, 1.5) / (495 + 0.0594 * Math.pow(sigma, 1.5));
        double beta_op = 3.348 * Math.pow(sigma, -0.8189);
        double A = 133 * Math.pow(sigma, -0.7913);

        return gamma_prime_max * Math.pow(beta / beta_op, A) * Math.exp(A * (A - beta / beta_op));
    }

    private double calculate_n_M() {
        double r_M = Math.min(M_f / M_x, 1.0);

        return 1 - 2.59 * r_M + 5.11 * r_M * r_M - 3.52 * r_M * r_M * r_M;
    }

    /**
     * @return xi The propagating flux ratio
     */
    private double calculate_xi() {

        return (1 / (192 + 0.2595 * sigma)) * Math.exp((0.792 + 0.681 * Math.pow(sigma, 0.5)) * (beta + 0.1));
    }

    /**
     * @return phi_w The wind factor
     */
    private double calculate_phi_w() {
        double C = 7.47 * Math.exp(-0.133 * Math.pow(sigma, 0.55));
        double B = 0.02526 * Math.pow(sigma, 0.54);
        double E = 0.715 * Math.exp(-3.59 * 1E-4 * sigma);
        double beta_op = 3.348 * Math.pow(sigma, -0.8189);

        return C * Math.pow(U, B) * Math.pow(beta / beta_op, -E);
    }

}