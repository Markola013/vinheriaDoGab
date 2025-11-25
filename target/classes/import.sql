-- This file allow to write SQL commands that will be emitted in test and dev.

-- Insere dados na tabela FichaVinho
insert into FichaVinho (descricaoAroma, tipoUva, harmonizacao) values(
    'Aroma intenso de frutas vermelhas maduras, com notas de baunilha e especiarias devido ao envelhecimento em carvalho.',
    'Cabernet Sauvignon',
    'Carnes vermelhas, massas com molho intenso e queijos maturados'
);

insert into FichaVinho (descricaoAroma, tipoUva, harmonizacao) values(
    'Aroma fresco e cítrico, com toques de maracujá e grapefruit. Acidez vibrante e final longo.',
    'Sauvignon Blanc',
    'Frutos do mar, saladas e queijos de cabra'
);

insert into FichaVinho (descricaoAroma, tipoUva, harmonizacao) values(
    'Aroma floral delicado, com notas de pêssego e damasco. Textura cremosa e levemente doce.',
    'Chardonnay',
    'Aves, risotos e culinária asiática'
);

insert into FichaVinho (descricaoAroma, tipoUva, harmonizacao) values(
    'Aroma de cerejas e amoras, com um toque terroso e de pimenta preta. Taninos suaves.',
    'Pinot Noir',
    'Massas leves, pizzas e carne de porco'
);

insert into FichaVinho (descricaoAroma, tipoUva, harmonizacao) values(
    'Aroma de frutas pretas escuras, tabaco e chocolate. Encorpado e com taninos firmes.',
    'Malbec',
    'Churrasco, hambúrgueres gourmet e comida mexicana'
);

-- Insere dados na tabela Vinho
insert into Vinho (nome, dataDeProducao, origem, ficha_vinho_id) values('Tinto Reserva', '2023-01-10', 'Chile', 1);
insert into Vinho (nome, dataDeProducao, origem, ficha_vinho_id) values('Branco Fresco', '2024-03-01', 'Nova Zelândia', 2);
insert into Vinho (nome, dataDeProducao, origem, ficha_vinho_id) values('Espumante Clássico', '2022-09-01', 'França', 3);
insert into Vinho (nome, dataDeProducao, origem, ficha_vinho_id) values('Rosé Leve', '2023-07-20', 'Itália', 4);
insert into Vinho (nome, dataDeProducao, origem, ficha_vinho_id) values('Malbec Premium', '2020-04-15', 'Argentina', 5);

-- Insere dados na tabela Varietal
insert into Varietal (nome, descricao) values('Blend', 'Mistura de duas ou mais uvas. Vinhos de personalidade complexa.');
insert into Varietal (nome, descricao) values('Cabernet Sauvignon', 'Varietal tinto de corpo robusto e notas de cassis e pimenta.');
insert into Varietal (nome, descricao) values('Chardonnay', 'Uva branca versátil, variando de leve e cítrica a encorpada e amanteigada.');
insert into Varietal (nome, descricao) values('Tempranillo', 'Uva tinta espanhola que produz vinhos com sabor de ameixa e taninos suaves.');
insert into Varietal (nome, descricao) values('Pinot Noir', 'Uva tinta delicada, com aromas de cereja, terroso e acidez alta.');

-- Insere dados na tabela PedidoVinho
insert into PedidoVinho (dataPedido, observacoes, status, vinho_id) values(
    '2025-01-20',
    'Quero um tinto encorpado para um jantar especial. Este reserva parece ser ideal.',
    'Pendente',
    1
);

insert into PedidoVinho (dataPedido, observacoes, status, vinho_id) values(
    '2025-01-21',
    'Minha esposa adora espumantes. Queremos experimentar este clássico na próxima celebração.',
    'Enviado',
    3
);

insert into PedidoVinho (dataPedido, observacoes, status, vinho_id) values(
    '2025-01-22',
    'Preciso de um vinho leve e refrescante para acompanhar frutos do mar na praia.',
    'Pendente',
    2
);

insert into PedidoVinho (dataPedido, observacoes, status, vinho_id) values(
    '2025-01-22',
    'Este Malbec será perfeito para o meu churrasco de domingo.',
    'Pendente',
    5
);

-- Associações pedidovinho-varietal (Many-to-Many)
insert into pedidovinho_varietal (pedidovinho_id, varietal_id) values (1, 1), (1, 2); -- Pedido 1, Blend e Cabernet Sauvignon
insert into pedidovinho_varietal (pedidovinho_id, varietal_id) values (2, 3);          -- Pedido 2, Chardonnay
insert into pedidovinho_varietal (pedidovinho_id, varietal_id) values (3, 2);          -- Pedido 3, Cabernet Sauvignon
insert into pedidovinho_varietal (pedidovinho_id, varietal_id) values (4, 5);          -- Pedido 4, Pinot Noir