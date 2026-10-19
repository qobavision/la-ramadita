-- V10: los productos que estaban como PLATO pasan a PLATO_EJECUTIVO.
-- No se borra ningún registro. productos.categoria no tiene CHECK; solo se actualiza el valor.

UPDATE dbo.productos
SET categoria = N'PLATO_EJECUTIVO'
WHERE categoria = N'PLATO';
