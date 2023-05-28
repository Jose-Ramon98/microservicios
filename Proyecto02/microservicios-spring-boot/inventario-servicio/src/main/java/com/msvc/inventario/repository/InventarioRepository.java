package com.msvc.inventario.repository;

import com.msvc.inventario.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario,Long> {



    Optional<Inventario> findByCodigoSkuIn(List<String> codigoSku);//Así no pasamos uno solo, pasamos una lista



}
