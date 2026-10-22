-- A versao 1 representa o esquema TB_* ja instalado pelo projeto Sprint 04 de Banco de Dados.
-- Esta migracao nao recria nem altera objetos: confirma que a aplicacao usa a conta Oracle correta.
DECLARE
    v_tables  NUMBER;
    v_objects NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_tables
      FROM USER_TABLES
     WHERE TABLE_NAME IN (
         'TB_USER', 'TB_PET', 'TB_DEVICE', 'TB_SENSOR_DATA', 'TB_ALERT',
         'TB_AUTH', 'TB_LOG_ERRO', 'TB_AUDITORIA_SENSOR', 'TB_ROLE',
         'TB_USER_ROLE', 'TB_PET_VETERINARIAN', 'TB_ALERT_HISTORY',
         'TB_PET_MESSAGE', 'TB_APPOINTMENT', 'TB_CARE_RECOMMENDATION'
     );

    IF v_tables <> 15 THEN
        RAISE_APPLICATION_ERROR(-20051,
            'Esquema PetCare360 incompleto: execute o script Sprint 04 na conta Oracle configurada.');
    END IF;

    SELECT COUNT(*) INTO v_objects
      FROM USER_OBJECTS
     WHERE STATUS = 'VALID'
       AND (
           (OBJECT_NAME IN ('PKG_PETCARE360', 'PKG_PETCARE360_CLINICO')
                AND OBJECT_TYPE IN ('PACKAGE', 'PACKAGE BODY'))
           OR (OBJECT_NAME = 'TRG_AUDITORIA_SENSOR' AND OBJECT_TYPE = 'TRIGGER')
       );

    IF v_objects <> 5 THEN
        RAISE_APPLICATION_ERROR(-20052,
            'Packages ou trigger PetCare360 ausentes/invalidos na conta Oracle configurada.');
    END IF;
END;
/
