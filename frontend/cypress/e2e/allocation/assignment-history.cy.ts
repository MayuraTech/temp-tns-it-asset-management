/**
 * E2E Test: Assignment History Viewing
 * 
 * This test verifies the complete workflow for viewing assignment history:
 * 1. Navigate to asset detail page
 * 2. View assignment history
 * 3. Verify history is displayed correctly
 */

describe('Assignment History Viewing Workflow', () => {
  // Test data
  const testAssetId = '550e8400-e29b-41d4-a716-446655440000';
  const adminUsername = 'admin';
  const adminPassword = 'Admin@123456';

  beforeEach(() => {
    // Login before each test
    cy.login(adminUsername, adminPassword);
  });

  afterEach(() => {
    // Logout after each test
    cy.logout();
  });

  describe('Navigate to Asset Detail', () => {
    it('should navigate to asset detail page from assets list', () => {
      // Navigate to assets page
      cy.visit('/assets');
      
      // Verify we're on the assets page
      cy.url().should('include', '/assets');
      cy.get('h1').should('contain', 'Assets');
      
      // Wait for assets to load
      cy.get('[data-cy=asset-list]', { timeout: 10000 }).should('be.visible');
      
      // Click on the first asset or search for specific asset
      cy.get('[data-cy=asset-row]').first().click();
      
      // Verify navigation to asset detail page
      cy.url().should('match', /\/assets\/[a-f0-9-]+$/);
      cy.get('[data-cy=asset-detail]').should('be.visible');
    });

    it('should navigate to asset detail page directly via URL', () => {
      // Navigate directly to asset detail page
      cy.visit(`/assets/${testAssetId}`);
      
      // Verify we're on the asset detail page
      cy.url().should('include', `/assets/${testAssetId}`);
      cy.get('[data-cy=asset-detail]').should('be.visible');
    });

    it('should display asset information on detail page', () => {
      // Navigate to asset detail page
      cy.visit(`/assets/${testAssetId}`);
      
      // Verify asset information is displayed
      cy.get('[data-cy=asset-detail]').within(() => {
        cy.get('[data-cy=asset-name]').should('be.visible');
        cy.get('[data-cy=asset-serial-number]').should('be.visible');
        cy.get('[data-cy=asset-type]').should('be.visible');
        cy.get('[data-cy=asset-status]').should('be.visible');
      });
    });

    it('should show assignment history section on asset detail page', () => {
      // Navigate to asset detail page
      cy.visit(`/assets/${testAssetId}`);
      
      // Verify assignment history section exists
      cy.get('[data-cy=assignment-history-section]').should('be.visible');
      cy.get('[data-cy=assignment-history-section]').within(() => {
        cy.get('h2').should('contain', 'Assignment History');
      });
    });
  });

  describe('View Assignment History', () => {
    beforeEach(() => {
      // Navigate to asset detail page before each test
      cy.visit(`/assets/${testAssetId}`);
    });

    it('should display assignment history table', () => {
      // Verify assignment history table is visible
      cy.get('[data-cy=assignment-history-table]').should('be.visible');
      
      // Verify table headers
      cy.get('[data-cy=assignment-history-table]').within(() => {
        cy.get('th').should('contain', 'Type');
        cy.get('th').should('contain', 'Assigned To');
        cy.get('th').should('contain', 'Assigned By');
        cy.get('th').should('contain', 'Assigned At');
        cy.get('th').should('contain', 'Unassigned At');
        cy.get('th').should('contain', 'Status');
      });
    });

    it('should display assignment history records', () => {
      // Wait for history data to load
      cy.get('[data-cy=assignment-history-table]').should('be.visible');
      
      // Verify at least one history record is displayed
      cy.get('[data-cy=history-row]').should('have.length.at.least', 1);
    });

    it('should display assignment type correctly', () => {
      // Verify assignment type is displayed
      cy.get('[data-cy=history-row]').first().within(() => {
        cy.get('[data-cy=assignment-type]').should('be.visible');
        cy.get('[data-cy=assignment-type]').should('match', /(User|Location)/);
      });
    });

    it('should display assigned to information', () => {
      // Verify assigned to field is displayed
      cy.get('[data-cy=history-row]').first().within(() => {
        cy.get('[data-cy=assigned-to]').should('be.visible');
        cy.get('[data-cy=assigned-to]').should('not.be.empty');
      });
    });

    it('should display assigned by username', () => {
      // Verify assigned by field is displayed
      cy.get('[data-cy=history-row]').first().within(() => {
        cy.get('[data-cy=assigned-by]').should('be.visible');
        cy.get('[data-cy=assigned-by]').should('not.be.empty');
      });
    });

    it('should display assignment dates', () => {
      // Verify assigned at date is displayed
      cy.get('[data-cy=history-row]').first().within(() => {
        cy.get('[data-cy=assigned-at]').should('be.visible');
        cy.get('[data-cy=assigned-at]').should('not.be.empty');
      });
    });

    it('should display assignment status (Active/Historical)', () => {
      // Verify status badge is displayed
      cy.get('[data-cy=history-row]').first().within(() => {
        cy.get('[data-cy=assignment-status]').should('be.visible');
        cy.get('[data-cy=assignment-status]').should('match', /(Active|Historical)/);
      });
    });

    it('should distinguish between active and historical assignments', () => {
      // Check if there are both active and historical assignments
      cy.get('[data-cy=history-row]').then(($rows) => {
        if ($rows.length > 1) {
          // Verify active assignment has no unassigned date
          cy.get('[data-cy=history-row]').each(($row) => {
            cy.wrap($row).within(() => {
              cy.get('[data-cy=assignment-status]').invoke('text').then((status) => {
                if (status.includes('Active')) {
                  cy.get('[data-cy=unassigned-at]').should('contain', 'N/A');
                } else if (status.includes('Historical')) {
                  cy.get('[data-cy=unassigned-at]').should('not.contain', 'N/A');
                }
              });
            });
          });
        }
      });
    });
  });

  describe('Verify History Display', () => {
    beforeEach(() => {
      // Navigate to asset detail page before each test
      cy.visit(`/assets/${testAssetId}`);
    });

    it('should display history in chronological order (most recent first)', () => {
      // Get all assigned at dates
      cy.get('[data-cy=history-row]').then(($rows) => {
        if ($rows.length > 1) {
          const dates: Date[] = [];
          
          $rows.each((index, row) => {
            const dateText = Cypress.$(row).find('[data-cy=assigned-at]').text();
            dates.push(new Date(dateText));
          });
          
          // Verify dates are in descending order
          for (let i = 0; i < dates.length - 1; i++) {
            expect(dates[i].getTime()).to.be.at.least(dates[i + 1].getTime());
          }
        }
      });
    });

    it('should display loading state while fetching history', () => {
      // Intercept the API call to delay response
      cy.intercept('GET', `/api/v1/assets/${testAssetId}/assignment-history*`, (req) => {
        req.reply((res) => {
          res.delay = 1000; // Delay response by 1 second
        });
      }).as('getHistory');
      
      // Navigate to asset detail page
      cy.visit(`/assets/${testAssetId}`);
      
      // Verify loading indicator is shown
      cy.get('[data-cy=history-loading]').should('be.visible');
      
      // Wait for API call to complete
      cy.wait('@getHistory');
      
      // Verify loading indicator is hidden
      cy.get('[data-cy=history-loading]').should('not.exist');
    });

    it('should display empty state when no history exists', () => {
      // Intercept the API call to return empty history
      cy.intercept('GET', `/api/v1/assets/${testAssetId}/assignment-history*`, {
        statusCode: 200,
        body: {
          content: [],
          page: {
            size: 20,
            number: 0,
            totalElements: 0,
            totalPages: 0
          }
        }
      }).as('getEmptyHistory');
      
      // Navigate to asset detail page
      cy.visit(`/assets/${testAssetId}`);
      
      // Wait for API call
      cy.wait('@getEmptyHistory');
      
      // Verify empty state is displayed
      cy.get('[data-cy=history-empty-state]').should('be.visible');
      cy.get('[data-cy=history-empty-state]').should('contain', 'No assignment history');
    });

    it('should display error message when history loading fails', () => {
      // Intercept the API call to return error
      cy.intercept('GET', `/api/v1/assets/${testAssetId}/assignment-history*`, {
        statusCode: 500,
        body: {
          error: {
            type: 'INTERNAL_SERVER_ERROR',
            message: 'Failed to load assignment history'
          }
        }
      }).as('getHistoryError');
      
      // Navigate to asset detail page
      cy.visit(`/assets/${testAssetId}`);
      
      // Wait for API call
      cy.wait('@getHistoryError');
      
      // Verify error message is displayed
      cy.get('[data-cy=history-error]').should('be.visible');
      cy.get('[data-cy=history-error]').should('contain', 'Failed to load');
    });

    it('should support pagination for large history', () => {
      // Check if pagination controls exist
      cy.get('[data-cy=assignment-history-table]').then(($table) => {
        // If pagination exists, test it
        cy.get('body').then(($body) => {
          if ($body.find('[data-cy=history-paginator]').length > 0) {
            // Verify paginator is visible
            cy.get('[data-cy=history-paginator]').should('be.visible');
            
            // Get total elements
            cy.get('[data-cy=history-paginator]').within(() => {
              cy.get('.mat-mdc-paginator-range-label').invoke('text').then((text) => {
                if (text.includes('of')) {
                  // Click next page if available
                  cy.get('button[aria-label="Next page"]').then(($btn) => {
                    if (!$btn.is(':disabled')) {
                      cy.wrap($btn).click();
                      
                      // Verify page changed
                      cy.get('.mat-mdc-paginator-range-label').should('not.contain', '1 – 20');
                    }
                  });
                }
              });
            });
          }
        });
      });
    });

    it('should display assignment type badges with correct colors', () => {
      // Verify assignment type badges have colors
      cy.get('[data-cy=history-row]').first().within(() => {
        cy.get('[data-cy=assignment-type]').should('have.class', 'mat-badge');
      });
    });

    it('should display status badges with correct colors', () => {
      // Verify status badges have colors
      cy.get('[data-cy=history-row]').first().within(() => {
        cy.get('[data-cy=assignment-status]').should('have.class', 'mat-badge');
      });
    });

    it('should format dates in readable format', () => {
      // Verify dates are formatted (not raw ISO strings)
      cy.get('[data-cy=history-row]').first().within(() => {
        cy.get('[data-cy=assigned-at]').invoke('text').then((dateText) => {
          // Should contain month name or formatted date
          expect(dateText).to.match(/(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|\d{1,2}\/\d{1,2}\/\d{4})/);
        });
      });
    });
  });

  describe('Complete Workflow: Navigate and View History', () => {
    it('should complete full workflow from assets list to viewing history', () => {
      // Step 1: Navigate to assets page
      cy.visit('/assets');
      cy.get('h1').should('contain', 'Assets');
      
      // Step 2: Click on an asset
      cy.get('[data-cy=asset-row]').first().click();
      
      // Step 3: Verify on asset detail page
      cy.url().should('match', /\/assets\/[a-f0-9-]+$/);
      cy.get('[data-cy=asset-detail]').should('be.visible');
      
      // Step 4: Verify assignment history section is visible
      cy.get('[data-cy=assignment-history-section]').should('be.visible');
      
      // Step 5: Verify history table is displayed
      cy.get('[data-cy=assignment-history-table]').should('be.visible');
      
      // Step 6: Verify history records are displayed
      cy.get('[data-cy=history-row]').should('have.length.at.least', 0);
      
      // Step 7: Verify all required columns are present
      cy.get('[data-cy=assignment-history-table]').within(() => {
        cy.get('th').should('contain', 'Type');
        cy.get('th').should('contain', 'Assigned To');
        cy.get('th').should('contain', 'Status');
      });
    });
  });

  describe('Authorization', () => {
    it('should allow ADMINISTRATOR to view assignment history', () => {
      // Already logged in as admin
      cy.visit(`/assets/${testAssetId}`);
      
      // Verify history is accessible
      cy.get('[data-cy=assignment-history-section]').should('be.visible');
      cy.get('[data-cy=assignment-history-table]').should('be.visible');
    });

    it('should allow ASSET_MANAGER to view assignment history', () => {
      // Logout and login as asset manager
      cy.logout();
      cy.login('assetmanager', 'AssetManager@123456');
      
      cy.visit(`/assets/${testAssetId}`);
      
      // Verify history is accessible
      cy.get('[data-cy=assignment-history-section]').should('be.visible');
      cy.get('[data-cy=assignment-history-table]').should('be.visible');
    });

    it('should allow VIEWER to view assignment history', () => {
      // Logout and login as viewer
      cy.logout();
      cy.login('viewer', 'Viewer@123456');
      
      cy.visit(`/assets/${testAssetId}`);
      
      // Verify history is accessible
      cy.get('[data-cy=assignment-history-section]').should('be.visible');
      cy.get('[data-cy=assignment-history-table]').should('be.visible');
    });
  });
});
