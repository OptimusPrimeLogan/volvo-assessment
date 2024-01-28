DROP TABLE IF EXISTS public_holidays;

CREATE TABLE public_holidays
(
  id INT AUTO_INCREMENT  PRIMARY KEY,
    month_year      TINYINT      NOT NULL,
    date_month      TINYINT NOT NULL
);

DROP TABLE IF EXISTS toll_fee_chart;

CREATE TABLE toll_fee_chart
(
  id INT AUTO_INCREMENT  PRIMARY KEY,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    price NUMERIC NOT NULL
);