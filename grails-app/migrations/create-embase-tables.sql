-- EMBASE_LITERATURE_ALERT
CREATE TABLE embase_literature_alert (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL,
    article_id BIGINT,
    article_authors TEXT,
    article_abstract TEXT,
    article_title TEXT,
    publication_date VARCHAR(255),
    name VARCHAR(255),
    product_selection TEXT,
    event_selection TEXT,
    search_string VARCHAR(8000),
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    action_count BIGINT,
    date_created TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    assigned_to_id BIGINT,
    priority_id BIGINT,
    disposition_id BIGINT,
    assigned_to_group_id BIGINT,
    disp_performed_by VARCHAR(255),
    is_disp_changed BOOLEAN DEFAULT FALSE,
    lit_search_config_id BIGINT NOT NULL,
    ex_lit_search_config_id BIGINT NOT NULL,
    global_identity_id BIGINT, 
    affiliation_organization TEXT,
    source_title TEXT,
    issn TEXT,
    source_link TEXT,
    publication_year VARCHAR(4),
    chemical_name TEXT,
    cas_registry_number TEXT,
    manufacturer TEXT,
    trade_name TEXT,
    trade_item_manufacturer TEXT,
    external_source VARCHAR(255),
    publisher_email VARCHAR(320),
    enzyme_commission_number TEXT,
    digital_object_identifier VARCHAR(100),
    explosion_groups_json TEXT,
    descriptor_groups_json TEXT,
    CONSTRAINT fk_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES pvuser(id),
    CONSTRAINT fk_priority FOREIGN KEY (priority_id) REFERENCES priority(id),
    CONSTRAINT fk_disposition FOREIGN KEY (disposition_id) REFERENCES disposition(id),
    CONSTRAINT fk_assigned_to_group FOREIGN KEY (assigned_to_group_id) REFERENCES groups(id),
    CONSTRAINT fk_literature_config FOREIGN KEY (lit_search_config_id) REFERENCES literature_config(id),
    CONSTRAINT fk_ex_literature_config FOREIGN KEY (ex_lit_search_config_id) REFERENCES ex_literature_config(id),
    CONSTRAINT fk_global_article FOREIGN KEY (global_identity_id) REFERENCES global_article("globalArticleId")
);


-- VALIDATED_EMBASE_LITERATURE_ALERTS
CREATE TABLE validated_embase_literature_alerts (
    validated_signal_id BIGINT NOT NULL, 
    embase_literature_alert_id BIGINT NOT NULL, 
    is_carry_forward BOOLEAN, 
    CONSTRAINT fk_embase_literature FOREIGN KEY (embase_literature_alert_id) REFERENCES embase_literature_alert(id),
    CONSTRAINT fk_vela_validated_signal FOREIGN KEY (validated_signal_id) REFERENCES validated_signal(id)
);


-- EMBASE_LITERATURE_CASE_ALERT_TAGS
CREATE TABLE embase_literature_case_alert_tags (
    embase_literature_alert_id BIGINT NOT NULL, 
    pvs_alert_tag_id BIGINT, 
    CONSTRAINT fk_elcat_embase_literature FOREIGN KEY (embase_literature_alert_id) REFERENCES embase_literature_alert(id)
);


-- EMBASE_LITERATURE_ALERT_TAGS
CREATE TABLE embase_literature_alert_tags (
    embase_literature_alert_id BIGINT NOT NULL, 
    alert_tag_id BIGINT, 
    CONSTRAINT fk_alert_tag FOREIGN KEY (alert_tag_id) REFERENCES alert_tag(id), 
    CONSTRAINT fk_elat_embase_literature FOREIGN KEY (embase_literature_alert_id) REFERENCES embase_literature_alert(id)
);


-- EMBASE_LITERATURE_ALERT_ACTIONS
CREATE TABLE embase_literature_alert_actions (
    embase_literature_alert_id BIGINT NOT NULL, 
    action_id BIGINT, 
    is_retained BOOLEAN DEFAULT FALSE, 
    CONSTRAINT fk_elaa_embase_literature FOREIGN KEY (embase_literature_alert_id) REFERENCES embase_literature_alert(id),
    CONSTRAINT fk_action FOREIGN KEY (action_id) REFERENCES actions(id)
);


-- ARCHIVED_EMBASE_LITERATURE_ALERT
CREATE TABLE archived_embase_literature_alert (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL,
    article_id BIGINT,
    article_authors TEXT,
    article_abstract TEXT,
    article_title TEXT,
    publication_date VARCHAR(255),
    name VARCHAR(255),
    product_selection TEXT,
    event_selection TEXT,
    search_string VARCHAR(8000),
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    action_count BIGINT,
    date_created TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    assigned_to_id BIGINT,
    priority_id BIGINT,
    disposition_id BIGINT,
    assigned_to_group_id BIGINT,
    disp_performed_by VARCHAR(255),
    is_disp_changed BOOLEAN DEFAULT FALSE,
    lit_search_config_id BIGINT NOT NULL,
    ex_lit_search_config_id BIGINT NOT NULL,
    global_identity_id BIGINT, 
    affiliation_organization TEXT,
    source_title TEXT,
    issn TEXT,
    source_link TEXT,
    publication_year VARCHAR(4),
    chemical_name TEXT,
    cas_registry_number TEXT,
    manufacturer TEXT,
    trade_name TEXT,
    trade_item_manufacturer TEXT,
    external_source VARCHAR(255),
    publisher_email VARCHAR(320),
    enzyme_commission_number TEXT,
    digital_object_identifier VARCHAR(100),
    explosion_groups_json TEXT,
    descriptor_groups_json TEXT,
    CONSTRAINT fk_aela_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES pvuser(id),
    CONSTRAINT fk_aela_priority FOREIGN KEY (priority_id) REFERENCES priority(id),
    CONSTRAINT fk_aela_disposition FOREIGN KEY (disposition_id) REFERENCES disposition(id),
    CONSTRAINT fk_aela_assigned_to_group FOREIGN KEY (assigned_to_group_id) REFERENCES groups(id),
    CONSTRAINT fk_aela_literature_config FOREIGN KEY (lit_search_config_id) REFERENCES literature_config(id),
    CONSTRAINT fk_aela_ex_literature_config FOREIGN KEY (ex_lit_search_config_id) REFERENCES ex_literature_config(id),
    CONSTRAINT fk_aela_global_article FOREIGN KEY (global_identity_id) REFERENCES global_article("globalArticleId")
);


-- ARCHIVED_EMBASE_LIT_ALERT_ACTIONS
CREATE TABLE archived_embase_lit_alert_actions (
    archived_embase_lit_alert_id BIGINT NOT NULL, 
    action_id BIGINT, 
    is_retained BOOLEAN DEFAULT FALSE, 
    CONSTRAINT fk_aelaa_action FOREIGN KEY (action_id) REFERENCES actions(id), 
    CONSTRAINT fk_aelaa_archived_embase_literature FOREIGN KEY (archived_embase_lit_alert_id) REFERENCES archived_embase_literature_alert(id)
);


-- VALIDATED_ARCHIVED_EMBASE_LIT_ALERTS
CREATE TABLE validated_archived_embase_lit_alerts (
    validated_signal_id BIGINT NOT NULL, 
    archived_embase_lit_alert_id BIGINT NOT NULL, 
    CONSTRAINT fk_vaela_validated_signal FOREIGN KEY (validated_signal_id) REFERENCES validated_signal(id), 
    CONSTRAINT fk_vaela_archived_embase_literature FOREIGN KEY (archived_embase_lit_alert_id) REFERENCES archived_embase_literature_alert(id)
);


-- ARCHIVED_EMBASE_LIT_CASE_ALERT_TAGS
CREATE TABLE archived_embase_lit_case_alert_tags (
    archived_embase_lit_alert_id BIGINT NOT NULL, 
    pvs_alert_tag_id BIGINT, 
    CONSTRAINT fk_aelcat_archived_embase_literature FOREIGN KEY (archived_embase_lit_alert_id) REFERENCES archived_embase_literature_alert(id), 
    CONSTRAINT fk_aelcat_alert_tag FOREIGN KEY (pvs_alert_tag_id) REFERENCES pvs_alert_tag(id)
);