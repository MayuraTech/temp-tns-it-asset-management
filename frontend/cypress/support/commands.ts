/// <reference types="cypress" />

/**
 * Custom Cypress commands for IT Asset Management E2E tests
 */

/**
 * Login command - authenticates a user and stores the JWT token
 * @param username - The username to login with
 * @param password - The password to login with
 * Login command - authenticates a user and stores the token
 */
Cypress.Commands.add('login', (username: string, password: string) => {
  cy.request({
    method: 'POST',
    url: 'http://localhost:8080/api/v1/auth/login',
    body: {
      username,
      password
    }
  }).then((response) => {
    expect(response.status).to.eq(200);
    expect(response.body).to.have.property('accessToken');
    
    // Store tokens in localStorage
    window.localStorage.setItem('accessToken', response.body.accessToken);
    window.localStorage.setItem('refreshToken', response.body.refreshToken);
  });
});

/**
 * Logout command - clears authentication tokens
 */
Cypress.Commands.add('logout', () => {
  window.localStorage.removeItem('accessToken');
  window.localStorage.removeItem('refreshToken');
});
