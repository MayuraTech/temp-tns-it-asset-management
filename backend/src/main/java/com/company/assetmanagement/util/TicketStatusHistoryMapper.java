package com.company.assetmanagement.util;

import com.company.assetmanagement.dto.TicketStatusHistoryDTO;
import com.company.assetmanagement.model.TicketStatusHistory;

/**
 * Utility class for mapping between TicketStatusHistory entity and TicketStatusHistoryDTO.
 * Provides static methods for entity-to-DTO conversions.
 */
public class TicketStatusHistoryMapper {
    
    private TicketStatusHistoryMapper() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Maps a TicketStatusHistory entity to a TicketStatusHistoryDTO.
     * 
     * @param history the ticket status history entity to map
     * @return the mapped TicketStatusHistoryDTO, or null if input is null
     */
    public static TicketStatusHistoryDTO toDTO(TicketStatusHistory history) {
        if (history == null) {
            return null;
        }
        
        TicketStatusHistoryDTO dto = new TicketStatusHistoryDTO();
        dto.setId(history.getId());
        dto.setTicketId(history.getTicketId());
        dto.setFromStatus(history.getFromStatus());
        dto.setToStatus(history.getToStatus());
        dto.setChangedBy(history.getChangedBy());
        dto.setChangedAt(history.getChangedAt());
        dto.setComments(history.getComments());
        
        return dto;
    }
    
    /**
     * Maps a TicketStatusHistory entity to a TicketStatusHistoryDTO with user name.
     * 
     * @param history the ticket status history entity to map
     * @param changedByName the name of the user who made the change
     * @return the mapped TicketStatusHistoryDTO, or null if input is null
     */
    public static TicketStatusHistoryDTO toDTO(TicketStatusHistory history, String changedByName) {
        if (history == null) {
            return null;
        }
        
        TicketStatusHistoryDTO dto = toDTO(history);
        dto.setChangedByName(changedByName);
        
        return dto;
    }
}
