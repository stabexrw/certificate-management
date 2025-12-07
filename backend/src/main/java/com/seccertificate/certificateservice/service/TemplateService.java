package com.seccertificate.certificateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.certificateservice.dto.PageResponse;
import com.seccertificate.certificateservice.dto.TemplateDTO;
import com.seccertificate.certificateservice.dto.TemplateSimulationRequest;
import com.seccertificate.certificateservice.dto.TemplateSimulationResponse;
import com.seccertificate.certificateservice.entity.Customer;
import com.seccertificate.certificateservice.entity.Template;
import com.seccertificate.certificateservice.exception.ResourceNotFoundException;
import com.seccertificate.certificateservice.repository.CustomerRepository;
import com.seccertificate.certificateservice.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {
    
    private final TemplateRepository templateRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
    
    @Transactional
    public TemplateDTO createTemplate(Long customerId, TemplateDTO templateDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        // Extract placeholders from template content
        List<String> extractedPlaceholders = extractPlaceholders(templateDTO.getTemplateContent());
        
        // Sanitize HTML templates to prevent XSS - but preserve placeholders
        String sanitizedContent = templateDTO.getTemplateContent();
        if (sanitizedContent != null && !sanitizedContent.isBlank()) {
            // Use relaxed safelist to preserve CKEditor formatting
            sanitizedContent = org.jsoup.Jsoup.clean(sanitizedContent, 
                org.jsoup.safety.Safelist.relaxed()
                    .addTags("span", "div", "style")
                    .addAttributes(":all", "style", "class", "id")
            );
        }

        Template template = Template.builder()
                .customer(customer)
                .name(templateDTO.getName())
                .description(templateDTO.getDescription())
                .templateContent(sanitizedContent)
                .type(Template.TemplateType.valueOf(templateDTO.getType()))
                .placeholders(serializePlaceholders(extractedPlaceholders))
                .status(Template.TemplateStatus.ACTIVE)
                .build();
        
        template = templateRepository.save(template);
        return mapToDTO(template);
    }
    
    @Transactional(readOnly = true)
    public TemplateDTO getTemplateById(Long customerId, Long templateId) {
        Template template = templateRepository.findByIdAndCustomerId(templateId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found or access denied"));
        return mapToDTO(template);
    }
    
    @Transactional(readOnly = true)
    public List<TemplateDTO> getTemplatesByCustomerId(Long customerId) {
        return templateRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PageResponse<TemplateDTO> getTemplatesPaginated(Long customerId, Pageable pageable) {
        Page<Template> page = templateRepository.findByCustomerId(customerId, pageable);
        
        return PageResponse.<TemplateDTO>builder()
                .content(page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
    
    @Transactional
    public TemplateDTO updateTemplate(Long customerId, Long templateId, TemplateDTO templateDTO) {
        Template template = templateRepository.findByIdAndCustomerId(templateId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found or access denied"));
        
        // Extract placeholders from updated content
        List<String> extractedPlaceholders = extractPlaceholders(templateDTO.getTemplateContent());
        
        template.setName(templateDTO.getName());
        template.setDescription(templateDTO.getDescription());
        // Sanitize HTML on update
        String sanitizedContent = templateDTO.getTemplateContent();
        if (sanitizedContent != null && !sanitizedContent.isBlank()) {
            sanitizedContent = org.jsoup.Jsoup.clean(sanitizedContent, 
                org.jsoup.safety.Safelist.relaxed()
                    .addTags("span", "div", "style")
                    .addAttributes(":all", "style", "class", "id")
            );
        }
        template.setTemplateContent(sanitizedContent);
        template.setPlaceholders(serializePlaceholders(extractedPlaceholders));
        
        template = templateRepository.save(template);
        return mapToDTO(template);
    }
    
    @Transactional
    public void deleteTemplate(Long customerId, Long templateId) {
        if (!templateRepository.existsByIdAndCustomerId(templateId, customerId)) {
            throw new ResourceNotFoundException("Template not found or access denied");
        }
        templateRepository.deleteById(templateId);
    }
    
    public void validateTemplateOwnership(Long customerId, Long templateId) {
        if (!templateRepository.existsByIdAndCustomerId(templateId, customerId)) {
            throw new AccessDeniedException("You don't have access to this template");
        }
    }
    
    /**
     * Extract placeholders from HTML content using regex pattern {{placeholder}}
     */
    public List<String> extractPlaceholders(String content) {
        if (content == null || content.isBlank()) {
            return new ArrayList<>();
        }
        
        Set<String> placeholders = new HashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            if (!placeholder.isEmpty()) {
                placeholders.add(placeholder);
            }
        }
        
        return new ArrayList<>(placeholders);
    }
    
    /**
     * Simulate certificate generation by replacing placeholders with test data
     */
    @Transactional(readOnly = true)
    public TemplateSimulationResponse simulateTemplate(Long customerId, TemplateSimulationRequest request) {
        Template template = templateRepository.findByIdAndCustomerId(request.getTemplateId(), customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found or access denied"));
        
        String templateContent = template.getTemplateContent();
        List<String> extractedPlaceholders = extractPlaceholders(templateContent);
        
        // Replace placeholders with provided values
        String previewHtml = replacePlaceholders(templateContent, request.getPlaceholderValues());
        
        return TemplateSimulationResponse.builder()
                .previewHtml(previewHtml)
                .extractedPlaceholders(extractedPlaceholders)
                .success(true)
                .message("Template simulation successful")
                .build();
    }
    
    /**
     * Replace all placeholders in content with provided values
     */
    public String replacePlaceholders(String content, Map<String, String> placeholderValues) {
        if (content == null || placeholderValues == null) {
            return content;
        }
        
        String result = content;
        for (Map.Entry<String, String> entry : placeholderValues.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    private String serializePlaceholders(List<String> placeholders) {
        try {
            return objectMapper.writeValueAsString(placeholders);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing placeholders", e);
        }
    }
    
    private List<String> deserializePlaceholders(String placeholders) {
        try {
            return objectMapper.readValue(placeholders, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
    
    private TemplateDTO mapToDTO(Template template) {
        return TemplateDTO.builder()
                .id(template.getId())
                .customerId(template.getCustomer().getId())
                .name(template.getName())
                .description(template.getDescription())
                .templateContent(template.getTemplateContent())
                .type(template.getType().name())
                .placeholders(deserializePlaceholders(template.getPlaceholders()))
                .status(template.getStatus().name())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}